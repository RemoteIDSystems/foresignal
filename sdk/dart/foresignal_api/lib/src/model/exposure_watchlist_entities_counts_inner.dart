//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_watchlist_entities_counts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureWatchlistEntitiesCountsInner {
  /// Returns a new [ExposureWatchlistEntitiesCountsInner] instance.
  ExposureWatchlistEntitiesCountsInner({

    required  this.watchlistId,

     this.name,

    required  this.count,
  });

  @JsonKey(
    
    name: r'watchlistId',
    required: true,
    includeIfNull: false,
  )


  final String watchlistId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureWatchlistEntitiesCountsInner &&
      other.watchlistId == watchlistId &&
      other.name == name &&
      other.count == count;

    @override
    int get hashCode =>
        watchlistId.hashCode +
        name.hashCode +
        count.hashCode;

  factory ExposureWatchlistEntitiesCountsInner.fromJson(Map<String, dynamic> json) => _$ExposureWatchlistEntitiesCountsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureWatchlistEntitiesCountsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

