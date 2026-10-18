//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:foresignal_api/src/model/sample_value_derived.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_sample.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesSample {
  /// Returns a new [SeriesSample] instance.
  SeriesSample({

    required  this.time,

    required  this.kind,

     this.value,

     this.u,

     this.v,

     this.derived,

     this.frameKey,

     this.run,

     this.step,

    required  this.status,

     this.statusText,
  });

  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final SeriesSampleKindEnum kind;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'u',
    required: false,
    includeIfNull: false,
  )


  final num? u;



  @JsonKey(
    
    name: r'v',
    required: false,
    includeIfNull: false,
  )


  final num? v;



  @JsonKey(
    
    name: r'derived',
    required: false,
    includeIfNull: false,
  )


  final SampleValueDerived? derived;



  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SampleStatus status;



  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesSample &&
      other.time == time &&
      other.kind == kind &&
      other.value == value &&
      other.u == u &&
      other.v == v &&
      other.derived == derived &&
      other.frameKey == frameKey &&
      other.run == run &&
      other.step == step &&
      other.status == status &&
      other.statusText == statusText;

    @override
    int get hashCode =>
        time.hashCode +
        kind.hashCode +
        (value == null ? 0 : value.hashCode) +
        (u == null ? 0 : u.hashCode) +
        (v == null ? 0 : v.hashCode) +
        (derived == null ? 0 : derived.hashCode) +
        (frameKey == null ? 0 : frameKey.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        status.hashCode +
        (statusText == null ? 0 : statusText.hashCode);

  factory SeriesSample.fromJson(Map<String, dynamic> json) => _$SeriesSampleFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesSampleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesSampleKindEnum {
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'forecast')
forecast(r'forecast');

const SeriesSampleKindEnum(this.value);

final String value;

@override
String toString() => value;
}


