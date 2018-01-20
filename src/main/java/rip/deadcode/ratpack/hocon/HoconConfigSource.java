package rip.deadcode.ratpack.hocon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.*;
import ratpack.config.ConfigSource;
import ratpack.file.FileSystemBinding;

import java.util.Map;

public final class HoconConfigSource implements ConfigSource {

    @Override
    public ObjectNode loadConfigData(
            ObjectMapper objectMapper, FileSystemBinding fileSystemBinding ) throws Exception {

        Config config = ConfigFactory.load();
        Config ratpackConfig = config.getConfig( "ratpack" );

        return walk( ratpackConfig.root(), objectMapper );
    }

    private static ObjectNode walk( ConfigObject config, ObjectMapper objectMapper ) {
        ObjectNode node = objectMapper.createObjectNode();

        for ( Map.Entry<String, ConfigValue> entry : config.entrySet() ) {

            switch ( entry.getValue().valueType() ) {
            case OBJECT:
                node.set( entry.getKey(), walk( (ConfigObject) entry.getValue(), objectMapper ) );
                break;
            case LIST:
                node.set( entry.getKey(), walkList( (ConfigList) entry.getValue(), objectMapper ) );
                break;
            case NUMBER:
                node.put( entry.getKey(), ( (Number) entry.getValue().unwrapped() ).longValue() );
                break;
            case BOOLEAN:
                node.put( entry.getKey(), (Boolean) entry.getValue().unwrapped() );
                break;
            case NULL:
                node.putNull( entry.getKey() );
                break;
            case STRING:
                node.put( entry.getKey(), (String) entry.getValue().unwrapped() );
                break;
            }
        }

        return node;
    }

    private static ArrayNode walkList( ConfigList config, ObjectMapper objectMapper ) {

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for ( ConfigValue each : config ) {
            switch ( each.valueType() ) {
            case OBJECT:
                arrayNode.add( walk( (ConfigObject) each, objectMapper ) );
                break;
            case LIST:
                arrayNode.add( walkList( config, objectMapper ) );
                break;
            case NUMBER:
                arrayNode.add( ( (Number) each.unwrapped() ).longValue() );
                break;
            case BOOLEAN:
                arrayNode.add( (Boolean) each.unwrapped() );
                break;
            case NULL:
                arrayNode.addNull();
                break;
            case STRING:
                arrayNode.add( (String) each.unwrapped() );
                break;
            }
        }

        return arrayNode;
    }

}
