import System.Environment (getArgs)
import Control.Distributed.Process
import Control.Distributed.Process.Node (initRemoteTable)
import Control.Distributed.Process.Backend.SimpleLocalnet
import Data.Map (Map)
import qualified Data.Map as Map (fromList)

import qualified CountWords
import qualified PolyDistrMapReduce
import qualified MonoDistrMapReduce

rtable :: RemoteTable
rtable = PolyDistrMapReduce.__remoteTable
       . MonoDistrMapReduce.__remoteTable
       . CountWords.__remoteTable
       $ initRemoteTable

main :: IO ()
main = do
  args <- getArgs

  case args of
    -- Local word count
    "local" : "count" : files -> do
      input <- constructInput files
      print $ CountWords.localCountWords input

    -- Distributed word count
    "master" : host : port : "count" : files -> do
      input   <- constructInput files
      backend <- initializeBackend host port rtable
      startMaster backend $ \slaves -> do
        result <- CountWords.distrCountWords slaves input
        liftIO $ print result


    -- Generic slave for distributed examples
    "slave" : host : port : [] -> do
      backend <- initializeBackend host port rtable
      startSlave backend

--------------------------------------------------------------------------------
-- Auxiliary                                                                  --
--------------------------------------------------------------------------------

constructInput :: [FilePath] -> IO (Map FilePath CountWords.Document)
constructInput files = do
  contents <- mapM readFile files
  return . Map.fromList $ zip files contents
