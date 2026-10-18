//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_streams.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoStreams {
  /// Returns a new [SystemInfoStreams] instance.
  SystemInfoStreams({

     this.clients,

     this.maxClients,

     this.changeSubscribers,

     this.alertSubscribers,

     this.eventSubscribers,
  });

      /// Open SSE clients
  @JsonKey(
    
    name: r'clients',
    required: false,
    includeIfNull: false,
  )


  final int? clients;



  @JsonKey(
    
    name: r'maxClients',
    required: false,
    includeIfNull: false,
  )


  final int? maxClients;



  @JsonKey(
    
    name: r'changeSubscribers',
    required: false,
    includeIfNull: false,
  )


  final int? changeSubscribers;



  @JsonKey(
    
    name: r'alertSubscribers',
    required: false,
    includeIfNull: false,
  )


  final int? alertSubscribers;



  @JsonKey(
    
    name: r'eventSubscribers',
    required: false,
    includeIfNull: false,
  )


  final int? eventSubscribers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoStreams &&
      other.clients == clients &&
      other.maxClients == maxClients &&
      other.changeSubscribers == changeSubscribers &&
      other.alertSubscribers == alertSubscribers &&
      other.eventSubscribers == eventSubscribers;

    @override
    int get hashCode =>
        clients.hashCode +
        maxClients.hashCode +
        changeSubscribers.hashCode +
        alertSubscribers.hashCode +
        eventSubscribers.hashCode;

  factory SystemInfoStreams.fromJson(Map<String, dynamic> json) => _$SystemInfoStreamsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoStreamsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

