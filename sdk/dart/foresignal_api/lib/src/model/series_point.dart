//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_point.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesPoint {
  /// Returns a new [SeriesPoint] instance.
  SeriesPoint({

    required  this.period,

    required  this.periodStart,

    required  this.periodEnd,

    required  this.value,

    required  this.unit,

    required  this.status,

     this.extra,

     this.version,

     this.observedAt,

     this.knownAt,

     this.coverageOk,
  });

      /// Upstream period label
  @JsonKey(
    
    name: r'period',
    required: true,
    includeIfNull: false,
  )


  final String period;



  @JsonKey(
    
    name: r'periodStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodStart;



  @JsonKey(
    
    name: r'periodEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime periodEnd;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final num? value;



  @JsonKey(
    
    name: r'unit',
    required: true,
    includeIfNull: false,
  )


  final String unit;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SeriesPointStatusEnum status;



      /// Flat scalar extras (e.g. `gasInStorageTWh`, `sharePct`)
  @JsonKey(
    
    name: r'extra',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object?>? extra;



      /// Record version; > 1 means the point was revised
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Ingest time of the version that supplied the point (via `seq_clock`); rollups use bucket close time, rule samples their write time
  @JsonKey(
    
    name: r'knownAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knownAt;



      /// False when the involved sources were failing or not covering the period
  @JsonKey(
    
    name: r'coverageOk',
    required: false,
    includeIfNull: false,
  )


  final bool? coverageOk;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesPoint &&
      other.period == period &&
      other.periodStart == periodStart &&
      other.periodEnd == periodEnd &&
      other.value == value &&
      other.unit == unit &&
      other.status == status &&
      other.extra == extra &&
      other.version == version &&
      other.observedAt == observedAt &&
      other.knownAt == knownAt &&
      other.coverageOk == coverageOk;

    @override
    int get hashCode =>
        period.hashCode +
        periodStart.hashCode +
        periodEnd.hashCode +
        (value == null ? 0 : value.hashCode) +
        unit.hashCode +
        status.hashCode +
        extra.hashCode +
        version.hashCode +
        observedAt.hashCode +
        knownAt.hashCode +
        coverageOk.hashCode;

  factory SeriesPoint.fromJson(Map<String, dynamic> json) => _$SeriesPointFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesPointToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesPointStatusEnum {
@JsonValue(r'final')
final_(r'final'),
@JsonValue(r'preliminary')
preliminary(r'preliminary'),
@JsonValue(r'estimated')
estimated(r'estimated'),
@JsonValue(r'forecast')
forecast(r'forecast');

const SeriesPointStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


