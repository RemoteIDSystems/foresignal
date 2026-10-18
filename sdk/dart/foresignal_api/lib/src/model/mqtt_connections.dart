//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mqtt_connections.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MqttConnections {
  /// Returns a new [MqttConnections] instance.
  MqttConnections({

    required  this.connections,

    required  this.connected,

     this.lastError,
  });

      /// Configured connections
  @JsonKey(
    
    name: r'connections',
    required: true,
    includeIfNull: false,
  )


  final int connections;



      /// Currently connected
  @JsonKey(
    
    name: r'connected',
    required: true,
    includeIfNull: false,
  )


  final int connected;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MqttConnections &&
      other.connections == connections &&
      other.connected == connected &&
      other.lastError == lastError;

    @override
    int get hashCode =>
        connections.hashCode +
        connected.hashCode +
        (lastError == null ? 0 : lastError.hashCode);

  factory MqttConnections.fromJson(Map<String, dynamic> json) => _$MqttConnectionsFromJson(json);

  Map<String, dynamic> toJson() => _$MqttConnectionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

