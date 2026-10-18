//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'map_style_metadata.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MapStyleMetadata {
  /// Returns a new [MapStyleMetadata] instance.
  MapStyleMetadata({

     this.foresignal,
  });

  @JsonKey(
    
    name: r'foresignal',
    required: false,
    includeIfNull: false,
  )


  final MapStyleMetadata? foresignal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MapStyleMetadata &&
      other.foresignal == foresignal;

    @override
    int get hashCode =>
        foresignal.hashCode;

  factory MapStyleMetadata.fromJson(Map<String, dynamic> json) => _$MapStyleMetadataFromJson(json);

  Map<String, dynamic> toJson() => _$MapStyleMetadataToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

