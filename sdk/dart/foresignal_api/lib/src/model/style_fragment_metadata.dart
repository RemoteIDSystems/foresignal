//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_fragment_metadata.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleFragmentMetadata {
  /// Returns a new [StyleFragmentMetadata] instance.
  StyleFragmentMetadata({

     this.legends,

     this.attribution,
  });

  @JsonKey(
    
    name: r'legends',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? legends;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StyleFragmentMetadata &&
      other.legends == legends &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        legends.hashCode +
        attribution.hashCode;

  factory StyleFragmentMetadata.fromJson(Map<String, dynamic> json) => _$StyleFragmentMetadataFromJson(json);

  Map<String, dynamic> toJson() => _$StyleFragmentMetadataToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

