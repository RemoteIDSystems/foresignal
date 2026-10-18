//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_social_volume_selector_scopes_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSocialVolumeSelectorScopesInner {
  /// Returns a new [ChronicleSocialVolumeSelectorScopesInner] instance.
  ChronicleSocialVolumeSelectorScopesInner({

    required  this.type,

    required  this.id,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSocialVolumeSelectorScopesInnerTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSocialVolumeSelectorScopesInner &&
      other.type == type &&
      other.id == id;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode;

  factory ChronicleSocialVolumeSelectorScopesInner.fromJson(Map<String, dynamic> json) => _$ChronicleSocialVolumeSelectorScopesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSocialVolumeSelectorScopesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSocialVolumeSelectorScopesInnerTypeEnum {
@JsonValue(r'topic')
topic(r'topic'),
@JsonValue(r'place')
place(r'place'),
@JsonValue(r'entity')
entity(r'entity');

const ChronicleSocialVolumeSelectorScopesInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


