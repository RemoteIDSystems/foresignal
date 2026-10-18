//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/raster_frame.dart';
import 'package:foresignal_api/src/model/raster_frame_page_prefetch.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFramePage {
  /// Returns a new [RasterFramePage] instance.
  RasterFramePage({

    required  this.items,

     this.nextCursor,

     this.prefetch,

    required  this.serverTime,

     this.earliest,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RasterFrame> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



  @JsonKey(
    
    name: r'prefetch',
    required: false,
    includeIfNull: false,
  )


  final RasterFramePagePrefetch? prefetch;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;



      /// Oldest retained frame
  @JsonKey(
    
    name: r'earliest',
    required: false,
    includeIfNull: false,
  )


  final DateTime? earliest;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RasterFramePage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.prefetch == prefetch &&
      other.serverTime == serverTime &&
      other.earliest == earliest;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        prefetch.hashCode +
        serverTime.hashCode +
        (earliest == null ? 0 : earliest.hashCode);

  factory RasterFramePage.fromJson(Map<String, dynamic> json) => _$RasterFramePageFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFramePageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

