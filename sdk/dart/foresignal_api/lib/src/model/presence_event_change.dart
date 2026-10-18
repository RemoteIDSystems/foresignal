//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'presence_event_change.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PresenceEventChange {
  /// Returns a new [PresenceEventChange] instance.
  PresenceEventChange({

    required  this.type,

    required  this.clientId,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PresenceEventChangeTypeEnum type;



  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final String clientId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PresenceEventChange &&
      other.type == type &&
      other.clientId == clientId;

    @override
    int get hashCode =>
        type.hashCode +
        clientId.hashCode;

  factory PresenceEventChange.fromJson(Map<String, dynamic> json) => _$PresenceEventChangeFromJson(json);

  Map<String, dynamic> toJson() => _$PresenceEventChangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PresenceEventChangeTypeEnum {
@JsonValue(r'joined')
joined(r'joined'),
@JsonValue(r'left')
left(r'left');

const PresenceEventChangeTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


