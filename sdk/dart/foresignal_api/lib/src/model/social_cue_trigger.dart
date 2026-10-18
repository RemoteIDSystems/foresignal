//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_trigger.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueTrigger {
  /// Returns a new [SocialCueTrigger] instance.
  SocialCueTrigger({

    required  this.type,

     this.ref,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SocialCueTriggerTypeEnum type;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueTrigger &&
      other.type == type &&
      other.ref == ref;

    @override
    int get hashCode =>
        type.hashCode +
        (ref == null ? 0 : ref.hashCode);

  factory SocialCueTrigger.fromJson(Map<String, dynamic> json) => _$SocialCueTriggerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueTriggerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialCueTriggerTypeEnum {
@JsonValue(r'situation-activated')
situationActivated(r'situation-activated'),
@JsonValue(r'situation-escalated')
situationEscalated(r'situation-escalated'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'anomaly')
anomaly(r'anomaly'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'canonical-event')
canonicalEvent(r'canonical-event'),
@JsonValue(r'manual')
manual(r'manual');

const SocialCueTriggerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


