//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_comparison_detectability.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialComparisonDetectability {
  /// Returns a new [SocialComparisonDetectability] instance.
  SocialComparisonDetectability({

    required  this.p,

    required  this.lo,

    required  this.hi,

    required  this.methods,

    required  this.ok,

     this.reason,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'p',
    required: true,
    includeIfNull: false,
  )


  final num p;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'lo',
    required: true,
    includeIfNull: false,
  )


  final num lo;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'hi',
    required: true,
    includeIfNull: false,
  )


  final num hi;



  @JsonKey(
    
    name: r'methods',
    required: true,
    includeIfNull: false,
  )


  final List<String> methods;



  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialComparisonDetectability &&
      other.p == p &&
      other.lo == lo &&
      other.hi == hi &&
      other.methods == methods &&
      other.ok == ok &&
      other.reason == reason;

    @override
    int get hashCode =>
        p.hashCode +
        lo.hashCode +
        hi.hashCode +
        methods.hashCode +
        ok.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory SocialComparisonDetectability.fromJson(Map<String, dynamic> json) => _$SocialComparisonDetectabilityFromJson(json);

  Map<String, dynamic> toJson() => _$SocialComparisonDetectabilityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

