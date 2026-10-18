//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/exposure_watchlist_entities_counts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_watchlist_entities.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureWatchlistEntities {
  /// Returns a new [ExposureWatchlistEntities] instance.
  ExposureWatchlistEntities({

    required  this.status,

     this.attribution,

     this.counts,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposureWatchlistEntitiesStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'counts',
    required: false,
    includeIfNull: false,
  )


  final List<ExposureWatchlistEntitiesCountsInner>? counts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureWatchlistEntities &&
      other.status == status &&
      other.attribution == attribution &&
      other.counts == counts;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        counts.hashCode;

  factory ExposureWatchlistEntities.fromJson(Map<String, dynamic> json) => _$ExposureWatchlistEntitiesFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureWatchlistEntitiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposureWatchlistEntitiesStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposureWatchlistEntitiesStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


