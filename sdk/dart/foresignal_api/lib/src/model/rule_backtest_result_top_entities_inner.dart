//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_top_entities_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultTopEntitiesInner {
  /// Returns a new [RuleBacktestResultTopEntitiesInner] instance.
  RuleBacktestResultTopEntitiesInner({

     this.sourceId,

     this.externalId,

     this.entityId,

    required  this.count,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'entityId',
    required: false,
    includeIfNull: false,
  )


  final String? entityId;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultTopEntitiesInner &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.entityId == entityId &&
      other.count == count;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        (entityId == null ? 0 : entityId.hashCode) +
        count.hashCode;

  factory RuleBacktestResultTopEntitiesInner.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultTopEntitiesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultTopEntitiesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

