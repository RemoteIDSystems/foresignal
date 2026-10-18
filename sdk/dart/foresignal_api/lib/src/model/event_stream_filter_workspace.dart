//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_filter_workspace.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamFilterWorkspace {
  /// Returns a new [EventStreamFilterWorkspace] instance.
  EventStreamFilterWorkspace({

    required  this.workspaceId,

     this.clientId,

     this.clientKind,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: true,
    includeIfNull: false,
  )


  final String workspaceId;



      /// This client's id; echoed in presence and used for `originClientId`
  @JsonKey(
    
    name: r'clientId',
    required: false,
    includeIfNull: false,
  )


  final String? clientId;



  @JsonKey(
    
    name: r'clientKind',
    required: false,
    includeIfNull: false,
  )


  final EventStreamFilterWorkspaceClientKindEnum? clientKind;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamFilterWorkspace &&
      other.workspaceId == workspaceId &&
      other.clientId == clientId &&
      other.clientKind == clientKind;

    @override
    int get hashCode =>
        workspaceId.hashCode +
        clientId.hashCode +
        clientKind.hashCode;

  factory EventStreamFilterWorkspace.fromJson(Map<String, dynamic> json) => _$EventStreamFilterWorkspaceFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamFilterWorkspaceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EventStreamFilterWorkspaceClientKindEnum {
@JsonValue(r'globe')
globe(r'globe'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'satellite')
satellite(r'satellite'),
@JsonValue(r'rf')
rf(r'rf'),
@JsonValue(r'mobile')
mobile(r'mobile'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'other')
other(r'other');

const EventStreamFilterWorkspaceClientKindEnum(this.value);

final String value;

@override
String toString() => value;
}


