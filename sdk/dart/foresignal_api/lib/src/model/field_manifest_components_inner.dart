//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_manifest_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldManifestComponentsInner {
  /// Returns a new [FieldManifestComponentsInner] instance.
  FieldManifestComponentsInner({

    required  this.name,

    required  this.offset,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final FieldManifestComponentsInnerNameEnum name;



      /// Byte offset in the .f32 data
  @JsonKey(
    
    name: r'offset',
    required: true,
    includeIfNull: false,
  )


  final int offset;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldManifestComponentsInner &&
      other.name == name &&
      other.offset == offset;

    @override
    int get hashCode =>
        name.hashCode +
        offset.hashCode;

  factory FieldManifestComponentsInner.fromJson(Map<String, dynamic> json) => _$FieldManifestComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$FieldManifestComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FieldManifestComponentsInnerNameEnum {
@JsonValue(r'u')
u(r'u'),
@JsonValue(r'v')
v(r'v');

const FieldManifestComponentsInnerNameEnum(this.value);

final String value;

@override
String toString() => value;
}


