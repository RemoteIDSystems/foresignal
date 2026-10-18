//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_catalog_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesCatalogEntry {
  /// Returns a new [SeriesCatalogEntry] instance.
  SeriesCatalogEntry({

    required  this.sourceId,

    required  this.series,

    required  this.seriesName,

     this.dataset,

    required  this.unit,

    required  this.frequency,

     this.area,

     this.latestPeriod,

     this.latestValue,

     this.status,

    required  this.family,

    required  this.transform,

    required  this.releaseLagDays,

    required  this.egress,

     this.regionCode,

     this.topicId,

     this.attribution,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final String series;



  @JsonKey(
    
    name: r'seriesName',
    required: true,
    includeIfNull: false,
  )


  final String seriesName;



  @JsonKey(
    
    name: r'dataset',
    required: false,
    includeIfNull: false,
  )


  final String? dataset;



      /// UCUM unit
  @JsonKey(
    
    name: r'unit',
    required: true,
    includeIfNull: false,
  )


  final String unit;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final SeriesCatalogEntryFrequencyEnum frequency;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final String? area;



  @JsonKey(
    
    name: r'latestPeriod',
    required: false,
    includeIfNull: false,
  )


  final String? latestPeriod;



  @JsonKey(
    
    name: r'latestValue',
    required: false,
    includeIfNull: false,
  )


  final num? latestValue;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final Object? status;



  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final SeriesCatalogEntryFamilyEnum family;



      /// Transform hint for analysis
  @JsonKey(
    
    name: r'transform',
    required: true,
    includeIfNull: false,
  )


  final SeriesCatalogEntryTransformEnum transform;



      /// Typical delay between period and publication; the version's ingest time is its knownAt
          // minimum: 0
  @JsonKey(
    
    name: r'releaseLagDays',
    required: true,
    includeIfNull: false,
  )


  final num releaseLagDays;



      /// The egress in force for the source (062; the operator override where the descriptor allows one); LOCAL_ONLY raw values are served only to callers allowed local egress, so such a series is absent from the catalog for other callers
  @JsonKey(
    
    name: r'egress',
    required: true,
    includeIfNull: false,
  )


  final SeriesCatalogEntryEgressEnum egress;



      /// 040 region code
  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



      /// Attention topic id
  @JsonKey(
    
    name: r'topicId',
    required: false,
    includeIfNull: false,
  )


  final String? topicId;



      /// The credit line the provider asks for: the FRED notice plus the series' source line for fred-series, the indicator's source organization for worldbank-indicators, else the source's attribution (117)
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesCatalogEntry &&
      other.sourceId == sourceId &&
      other.series == series &&
      other.seriesName == seriesName &&
      other.dataset == dataset &&
      other.unit == unit &&
      other.frequency == frequency &&
      other.area == area &&
      other.latestPeriod == latestPeriod &&
      other.latestValue == latestValue &&
      other.status == status &&
      other.family == family &&
      other.transform == transform &&
      other.releaseLagDays == releaseLagDays &&
      other.egress == egress &&
      other.regionCode == regionCode &&
      other.topicId == topicId &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        sourceId.hashCode +
        series.hashCode +
        seriesName.hashCode +
        (dataset == null ? 0 : dataset.hashCode) +
        unit.hashCode +
        frequency.hashCode +
        (area == null ? 0 : area.hashCode) +
        (latestPeriod == null ? 0 : latestPeriod.hashCode) +
        (latestValue == null ? 0 : latestValue.hashCode) +
        (status == null ? 0 : status.hashCode) +
        family.hashCode +
        transform.hashCode +
        releaseLagDays.hashCode +
        egress.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode) +
        (topicId == null ? 0 : topicId.hashCode) +
        (attribution == null ? 0 : attribution.hashCode);

  factory SeriesCatalogEntry.fromJson(Map<String, dynamic> json) => _$SeriesCatalogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesCatalogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesCatalogEntryFrequencyEnum {
@JsonValue(r'PT15S')
PT15S(r'PT15S'),
@JsonValue(r'PT15M')
PT15M(r'PT15M'),
@JsonValue(r'PT30M')
PT30M(r'PT30M'),
@JsonValue(r'PT1H')
PT1H(r'PT1H'),
@JsonValue(r'P1D')
P1D(r'P1D'),
@JsonValue(r'P1W')
P1W(r'P1W'),
@JsonValue(r'P1M')
P1M(r'P1M'),
@JsonValue(r'P3M')
P3M(r'P3M'),
@JsonValue(r'P1Y')
P1Y(r'P1Y');

const SeriesCatalogEntryFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SeriesCatalogEntryFamilyEnum {
@JsonValue(r'price')
price(r'price'),
@JsonValue(r'volume')
volume(r'volume'),
@JsonValue(r'open-interest')
openInterest(r'open-interest'),
@JsonValue(r'position')
position(r'position'),
@JsonValue(r'rate')
rate(r'rate'),
@JsonValue(r'attention')
attention(r'attention'),
@JsonValue(r'tone')
tone(r'tone'),
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'index')
indexValue(r'index'),
@JsonValue(r'structural')
structural(r'structural');

const SeriesCatalogEntryFamilyEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Transform hint for analysis
enum SeriesCatalogEntryTransformEnum {
@JsonValue(r'log-return')
logReturn(r'log-return'),
@JsonValue(r'log-level')
logLevel(r'log-level'),
@JsonValue(r'difference')
difference(r'difference'),
@JsonValue(r'level')
level(r'level');

const SeriesCatalogEntryTransformEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The egress in force for the source (062; the operator override where the descriptor allows one); LOCAL_ONLY raw values are served only to callers allowed local egress, so such a series is absent from the catalog for other callers
enum SeriesCatalogEntryEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const SeriesCatalogEntryEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


