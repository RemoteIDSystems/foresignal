//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_encodings_i16.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldEncodingsI16 {
  /// Returns a new [FieldEncodingsI16] instance.
  FieldEncodingsI16({

     this.url,

     this.scale,

     this.offset,
  });

  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'scale',
    required: false,
    includeIfNull: false,
  )


  final num? scale;



  @JsonKey(
    
    name: r'offset',
    required: false,
    includeIfNull: false,
  )


  final num? offset;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldEncodingsI16 &&
      other.url == url &&
      other.scale == scale &&
      other.offset == offset;

    @override
    int get hashCode =>
        url.hashCode +
        scale.hashCode +
        offset.hashCode;

  factory FieldEncodingsI16.fromJson(Map<String, dynamic> json) => _$FieldEncodingsI16FromJson(json);

  Map<String, dynamic> toJson() => _$FieldEncodingsI16ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

