//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backtest_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BacktestRequest {
  /// Returns a new [BacktestRequest] instance.
  BacktestRequest({

    required  this.type,

     this.targetId,

     this.definition,

     this.from,

     this.to,

     this.fromSeq,

     this.toSeq,

     this.variants,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final BacktestRequestTypeEnum type;



      /// Rule id, recipe id or precursor test id
  @JsonKey(
    
    name: r'targetId',
    required: false,
    includeIfNull: false,
  )


  final String? targetId;



      /// Inline definition (TriggerRuleInput, recipe definition or PrecursorTestInput) instead of targetId
  @JsonKey(
    
    name: r'definition',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? definition;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



      /// Rule backtests only; run in the same pass
  @JsonKey(
    
    name: r'variants',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? variants;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BacktestRequest &&
      other.type == type &&
      other.targetId == targetId &&
      other.definition == definition &&
      other.from == from &&
      other.to == to &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.variants == variants;

    @override
    int get hashCode =>
        type.hashCode +
        targetId.hashCode +
        definition.hashCode +
        from.hashCode +
        to.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        variants.hashCode;

  factory BacktestRequest.fromJson(Map<String, dynamic> json) => _$BacktestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$BacktestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BacktestRequestTypeEnum {
@JsonValue(r'rule')
rule(r'rule'),
@JsonValue(r'recipe')
recipe(r'recipe'),
@JsonValue(r'precursor')
precursor(r'precursor'),
@JsonValue(r'quality')
quality(r'quality');

const BacktestRequestTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


