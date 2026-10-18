//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_velocity_event_scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVelocityEventScope {
  /// Returns a new [SocialVelocityEventScope] instance.
  SocialVelocityEventScope({

    required  this.type,

    required  this.id,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SocialVelocityEventScopeTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVelocityEventScope &&
      other.type == type &&
      other.id == id;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode;

  factory SocialVelocityEventScope.fromJson(Map<String, dynamic> json) => _$SocialVelocityEventScopeFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVelocityEventScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialVelocityEventScopeTypeEnum {
@JsonValue(r'topic')
topic(r'topic'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'entity')
entity(r'entity');

const SocialVelocityEventScopeTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


