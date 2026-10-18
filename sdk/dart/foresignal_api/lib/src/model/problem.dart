//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/problem_errors_inner.dart';
import 'package:foresignal_api/src/model/problem_references.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'problem.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Problem {
  /// Returns a new [Problem] instance.
  Problem({

     this.type,

    required  this.title,

    required  this.status,

     this.detail,

     this.instance,

     this.code,

     this.errors,

     this.purgeHorizon,

     this.watermarkSeq,

     this.horizon,

     this.capUsd,

     this.spentUsd,

     this.estimateUsd,

     this.requiredScope,

     this.class_,

     this.retryAfterSeconds,

     this.references,

     this.reason,
  });

  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final int status;



  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final String? detail;



  @JsonKey(
    
    name: r'instance',
    required: false,
    includeIfNull: false,
  )


  final String? instance;



      /// Stable machine code
  @JsonKey(
    
    name: r'code',
    required: false,
    includeIfNull: false,
  )


  final String? code;



  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final List<ProblemErrorsInner>? errors;



      /// RESYNC_REQUIRED: purge horizon seq
  @JsonKey(
    
    name: r'purgeHorizon',
    required: false,
    includeIfNull: false,
  )


  final int? purgeHorizon;



      /// RESYNC_REQUIRED: current watermark
  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



      /// ASOF_BEYOND_RETENTION: earliest supported asOf
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



      /// BUDGET_EXCEEDED: monthly money cap
  @JsonKey(
    
    name: r'capUsd',
    required: false,
    includeIfNull: false,
  )


  final num? capUsd;



      /// BUDGET_EXCEEDED: spent this month
  @JsonKey(
    
    name: r'spentUsd',
    required: false,
    includeIfNull: false,
  )


  final num? spentUsd;



      /// BUDGET_EXCEEDED: estimated cost of the refused call
  @JsonKey(
    
    name: r'estimateUsd',
    required: false,
    includeIfNull: false,
  )


  final num? estimateUsd;



      /// INSUFFICIENT_SCOPE: the scope the operation needs (023)
  @JsonKey(
    
    name: r'requiredScope',
    required: false,
    includeIfNull: false,
  )


  final ProblemRequiredScopeEnum? requiredScope;



      /// RATE_LIMITED: the rate-limit class that refused the request (035)
  @JsonKey(
    
    name: r'class',
    required: false,
    includeIfNull: false,
  )


  final ProblemClass_Enum? class_;



      /// RATE_LIMITED: seconds to wait, as in Retry-After (035)
  @JsonKey(
    
    name: r'retryAfterSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? retryAfterSeconds;



  @JsonKey(
    
    name: r'references',
    required: false,
    includeIfNull: false,
  )


  final ProblemReferences? references;



      /// VALIDATION_FAILED from ImageGuard (043): the refusal (file-too-large, unknown-format, bigtiff, content-type-mismatch, truncated, side-too-long, too-many-pixels, multi-image, unreadable)
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Problem &&
      other.type == type &&
      other.title == title &&
      other.status == status &&
      other.detail == detail &&
      other.instance == instance &&
      other.code == code &&
      other.errors == errors &&
      other.purgeHorizon == purgeHorizon &&
      other.watermarkSeq == watermarkSeq &&
      other.horizon == horizon &&
      other.capUsd == capUsd &&
      other.spentUsd == spentUsd &&
      other.estimateUsd == estimateUsd &&
      other.requiredScope == requiredScope &&
      other.class_ == class_ &&
      other.retryAfterSeconds == retryAfterSeconds &&
      other.references == references &&
      other.reason == reason;

    @override
    int get hashCode =>
        type.hashCode +
        title.hashCode +
        status.hashCode +
        detail.hashCode +
        instance.hashCode +
        code.hashCode +
        errors.hashCode +
        (purgeHorizon == null ? 0 : purgeHorizon.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        (horizon == null ? 0 : horizon.hashCode) +
        (capUsd == null ? 0 : capUsd.hashCode) +
        (spentUsd == null ? 0 : spentUsd.hashCode) +
        (estimateUsd == null ? 0 : estimateUsd.hashCode) +
        (requiredScope == null ? 0 : requiredScope.hashCode) +
        (class_ == null ? 0 : class_.hashCode) +
        (retryAfterSeconds == null ? 0 : retryAfterSeconds.hashCode) +
        (references == null ? 0 : references.hashCode) +
        (reason == null ? 0 : reason.hashCode);

  factory Problem.fromJson(Map<String, dynamic> json) => _$ProblemFromJson(json);

  Map<String, dynamic> toJson() => _$ProblemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// INSUFFICIENT_SCOPE: the scope the operation needs (023)
enum ProblemRequiredScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const ProblemRequiredScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// RATE_LIMITED: the rate-limit class that refused the request (035)
enum ProblemClass_Enum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'tiles')
tiles(r'tiles'),
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'expensive')
expensive(r'expensive'),
@JsonValue(r'write')
write(r'write'),
@JsonValue(r'anonymous')
anonymous(r'anonymous'),
@JsonValue(r'metrics')
metrics(r'metrics');

const ProblemClass_Enum(this.value);

final String value;

@override
String toString() => value;
}


