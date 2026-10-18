//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analog_result_outcome_distribution.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/analog.dart';
import 'package:foresignal_api/src/model/analog_result_signature.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/analog_result_target.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogResult {
  /// Returns a new [AnalogResult] instance.
  AnalogResult({

    required  this.target,

    required  this.k,

    required  this.countFound,

    required  this.method,

     this.methodNote,

     this.signature,

    required  this.analogs,

     this.outcomeDistribution,

    required  this.caveats,

    required  this.detector,

    required  this.manifestId,
  });

  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final AnalogResultTarget target;



  @JsonKey(
    
    name: r'k',
    required: true,
    includeIfNull: false,
  )


  final int k;



      /// Analogs found before truncation to k
  @JsonKey(
    
    name: r'countFound',
    required: true,
    includeIfNull: false,
  )


  final int countFound;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final AnalogResultMethodEnum method;



      /// e.g. `feature similarity only: embeddings unavailable`
  @JsonKey(
    
    name: r'methodNote',
    required: false,
    includeIfNull: false,
  )


  final String? methodNote;



  @JsonKey(
    
    name: r'signature',
    required: false,
    includeIfNull: false,
  )


  final AnalogResultSignature? signature;



  @JsonKey(
    
    name: r'analogs',
    required: true,
    includeIfNull: false,
  )


  final List<Analog> analogs;



  @JsonKey(
    
    name: r'outcomeDistribution',
    required: false,
    includeIfNull: false,
  )


  final AnalogResultOutcomeDistribution? outcomeDistribution;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogResult &&
      other.target == target &&
      other.k == k &&
      other.countFound == countFound &&
      other.method == method &&
      other.methodNote == methodNote &&
      other.signature == signature &&
      other.analogs == analogs &&
      other.outcomeDistribution == outcomeDistribution &&
      other.caveats == caveats &&
      other.detector == detector &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        target.hashCode +
        k.hashCode +
        countFound.hashCode +
        method.hashCode +
        (methodNote == null ? 0 : methodNote.hashCode) +
        (signature == null ? 0 : signature.hashCode) +
        analogs.hashCode +
        (outcomeDistribution == null ? 0 : outcomeDistribution.hashCode) +
        caveats.hashCode +
        detector.hashCode +
        manifestId.hashCode;

  factory AnalogResult.fromJson(Map<String, dynamic> json) => _$AnalogResultFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalogResultMethodEnum {
@JsonValue(r'fused')
fused(r'fused'),
@JsonValue(r'feature-only')
featureOnly(r'feature-only');

const AnalogResultMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


