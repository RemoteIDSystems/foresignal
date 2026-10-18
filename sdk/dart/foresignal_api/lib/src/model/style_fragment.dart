//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/style_fragment_metadata.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style_fragment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StyleFragment {
  /// Returns a new [StyleFragment] instance.
  StyleFragment({

    required  this.version,

    required  this.sources,

    required  this.layers,

     this.metadata,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final StyleFragmentVersionEnum version;



  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>> sources;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<Map<String, Object>> layers;



  @JsonKey(
    
    name: r'metadata',
    required: false,
    includeIfNull: false,
  )


  final StyleFragmentMetadata? metadata;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StyleFragment &&
      other.version == version &&
      other.sources == sources &&
      other.layers == layers &&
      other.metadata == metadata;

    @override
    int get hashCode =>
        version.hashCode +
        sources.hashCode +
        layers.hashCode +
        metadata.hashCode;

  factory StyleFragment.fromJson(Map<String, dynamic> json) => _$StyleFragmentFromJson(json);

  Map<String, dynamic> toJson() => _$StyleFragmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StyleFragmentVersionEnum {
@JsonValue(8)
number8('8');

const StyleFragmentVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


