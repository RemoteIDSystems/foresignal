//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'shared_content_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SharedContentLinks {
  /// Returns a new [SharedContentLinks] instance.
  SharedContentLinks({

     this.geojson,

     this.tiles,

     this.download,
  });

  @JsonKey(
    
    name: r'geojson',
    required: false,
    includeIfNull: false,
  )


  final String? geojson;



      /// Tile URL template (views and tracks)
  @JsonKey(
    
    name: r'tiles',
    required: false,
    includeIfNull: false,
  )


  final String? tiles;



      /// Exports only
  @JsonKey(
    
    name: r'download',
    required: false,
    includeIfNull: false,
  )


  final String? download;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SharedContentLinks &&
      other.geojson == geojson &&
      other.tiles == tiles &&
      other.download == download;

    @override
    int get hashCode =>
        (geojson == null ? 0 : geojson.hashCode) +
        (tiles == null ? 0 : tiles.hashCode) +
        (download == null ? 0 : download.hashCode);

  factory SharedContentLinks.fromJson(Map<String, dynamic> json) => _$SharedContentLinksFromJson(json);

  Map<String, dynamic> toJson() => _$SharedContentLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

