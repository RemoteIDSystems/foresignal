//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/series_point.dart';
import 'package:foresignal_api/src/model/series_catalog_entry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_point_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesPointPage {
  /// Returns a new [SeriesPointPage] instance.
  SeriesPointPage({

    required  this.series,

    required  this.items,

     this.nextCursor,

     this.truncated,
  });

  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final SeriesCatalogEntry series;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesPoint> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



      /// More points exist in [from, to) than `limit`
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesPointPage &&
      other.series == series &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        series.hashCode +
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        truncated.hashCode;

  factory SeriesPointPage.fromJson(Map<String, dynamic> json) => _$SeriesPointPageFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesPointPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

