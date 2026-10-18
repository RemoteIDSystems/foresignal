//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'budget.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Budget {
  /// Returns a new [Budget] instance.
  Budget({

    required  this.perMinute,

    required  this.perDay,

     this.monthlyCapUsd,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'perMinute',
    required: true,
    includeIfNull: false,
  )


  final int perMinute;



          // minimum: 0
  @JsonKey(
    
    name: r'perDay',
    required: true,
    includeIfNull: false,
  )


  final int perDay;



      /// Money cap per calendar month for paid adapters (135; X default 25); refused with BUDGET_EXCEEDED
          // minimum: 0
  @JsonKey(
    
    name: r'monthlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? monthlyCapUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Budget &&
      other.perMinute == perMinute &&
      other.perDay == perDay &&
      other.monthlyCapUsd == monthlyCapUsd;

    @override
    int get hashCode =>
        perMinute.hashCode +
        perDay.hashCode +
        (monthlyCapUsd == null ? 0 : monthlyCapUsd.hashCode);

  factory Budget.fromJson(Map<String, dynamic> json) => _$BudgetFromJson(json);

  Map<String, dynamic> toJson() => _$BudgetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

