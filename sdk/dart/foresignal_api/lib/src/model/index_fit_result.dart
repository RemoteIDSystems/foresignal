//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/index_fit_result_components_inner.dart';
import 'package:foresignal_api/src/model/regional_index_def_skill.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'index_fit_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndexFitResult {
  /// Returns a new [IndexFitResult] instance.
  IndexFitResult({

    required  this.id,

    required  this.indexId,

    required  this.status,

     this.startedAt,

     this.finishedAt,

     this.method = 'L2-regularised NB regression, rolling origin',

     this.lambda,

     this.regions,

     this.origins,

     this.proposedWeights,

     this.weightsVersion,

     this.skill,

     this.components,

     this.applied,

     this.appliedAt,

     this.manifestId,

     this.error,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'indexId',
    required: true,
    includeIfNull: false,
  )


  final String indexId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final IndexFitResultStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    defaultValue: 'L2-regularised NB regression, rolling origin',
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



  @JsonKey(
    
    name: r'lambda',
    required: false,
    includeIfNull: false,
  )


  final num? lambda;



  @JsonKey(
    
    name: r'regions',
    required: false,
    includeIfNull: false,
  )


  final int? regions;



  @JsonKey(
    
    name: r'origins',
    required: false,
    includeIfNull: false,
  )


  final int? origins;



  @JsonKey(
    
    name: r'proposedWeights',
    required: false,
    includeIfNull: false,
  )


  final Object? proposedWeights;



      /// Version to pass to the weights operation
  @JsonKey(
    
    name: r'weightsVersion',
    required: false,
    includeIfNull: false,
  )


  final int? weightsVersion;



  @JsonKey(
    
    name: r'skill',
    required: false,
    includeIfNull: false,
  )


  final RegionalIndexDefSkill? skill;



  @JsonKey(
    
    name: r'components',
    required: false,
    includeIfNull: false,
  )


  final List<IndexFitResultComponentsInner>? components;



  @JsonKey(
    
    name: r'applied',
    required: false,
    includeIfNull: false,
  )


  final bool? applied;



  @JsonKey(
    
    name: r'appliedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? appliedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndexFitResult &&
      other.id == id &&
      other.indexId == indexId &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.method == method &&
      other.lambda == lambda &&
      other.regions == regions &&
      other.origins == origins &&
      other.proposedWeights == proposedWeights &&
      other.weightsVersion == weightsVersion &&
      other.skill == skill &&
      other.components == components &&
      other.applied == applied &&
      other.appliedAt == appliedAt &&
      other.manifestId == manifestId &&
      other.error == error;

    @override
    int get hashCode =>
        id.hashCode +
        indexId.hashCode +
        status.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        method.hashCode +
        (lambda == null ? 0 : lambda.hashCode) +
        (regions == null ? 0 : regions.hashCode) +
        (origins == null ? 0 : origins.hashCode) +
        (proposedWeights == null ? 0 : proposedWeights.hashCode) +
        (weightsVersion == null ? 0 : weightsVersion.hashCode) +
        (skill == null ? 0 : skill.hashCode) +
        components.hashCode +
        applied.hashCode +
        (appliedAt == null ? 0 : appliedAt.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory IndexFitResult.fromJson(Map<String, dynamic> json) => _$IndexFitResultFromJson(json);

  Map<String, dynamic> toJson() => _$IndexFitResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndexFitResultStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed');

const IndexFitResultStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


