# srds (Systemy Rozproszone Dużej Skali - Large Scale Distributed Systems)

Project written in Java + Cassandra. 

For Cassandra to work on multiple endpoints first delete <cassandra>/data directory, then edit: 
<cassandra>/conf/cassandra.yaml:
cluster_name: ’any_name’
num_tokens: 256
seed_provider:
- class_name: org.apache.cassandra.locator.SimpleSeedProvider
parameters:
- seeds: "list_of_IP_addresses"
listen_address: my_(local)_IP
rpc_address: localhost
endpoint_snitch: SimpleSnitch

Launch MapReader and Viewer, then main EvacuationSimulator with proper app arguments.
