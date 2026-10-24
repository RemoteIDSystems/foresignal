//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'claim_reputation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClaimReputation {
  /// Returns a new [ClaimReputation] instance.
  ClaimReputation({

    required  this.outletId,

    required  this.factuality,

     this.lean,

    required  this.stateAffiliated,
  });

      /// The rated outlet the source resolved to
  @JsonKey(
    
    name: r'outletId',
    required: true,
    includeIfNull: false,
  )


  final String outletId;



      /// The outlet's factuality grade (A best, F worst)
  @JsonKey(
    
    name: r'factuality',
    required: true,
    includeIfNull: false,
  )


  final String factuality;



      /// Consensus partisan lean bucket (e.g. left / center / right), or null when unrated
  @JsonKey(
    
    name: r'lean',
    required: false,
    includeIfNull: false,
  )


  final String? lean;



      /// Whether the outlet is state media
  @JsonKey(
    
    name: r'stateAffiliated',
    required: true,
    includeIfNull: false,
  )


  final bool stateAffiliated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClaimReputation &&
      other.outletId == outletId &&
      other.factuality == factuality &&
      other.lean == lean &&
      other.stateAffiliated == stateAffiliated;

    @override
    int get hashCode =>
        outletId.hashCode +
        factuality.hashCode +
        (lean == null ? 0 : lean.hashCode) +
        stateAffiliated.hashCode;

  factory ClaimReputation.fromJson(Map<String, dynamic> json) => _$ClaimReputationFromJson(json);

  Map<String, dynamic> toJson() => _$ClaimReputationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

