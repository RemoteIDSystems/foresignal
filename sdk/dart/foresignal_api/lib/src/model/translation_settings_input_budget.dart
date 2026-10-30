//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'translation_settings_input_budget.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TranslationSettingsInputBudget {
  /// Returns a new [TranslationSettingsInputBudget] instance.
  TranslationSettingsInputBudget({

     this.window,

     this.capUsd,
  });

      /// The UTC window the cap applies to
  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final TranslationSettingsInputBudgetWindowEnum? window;



      /// USD translation may spend per window, or null for no separate cap; once spent, articles are left untranslated (and recorded as skipped) until the window resets
          // minimum: 0
  @JsonKey(
    
    name: r'capUsd',
    required: false,
    includeIfNull: false,
  )


  final num? capUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TranslationSettingsInputBudget &&
      other.window == window &&
      other.capUsd == capUsd;

    @override
    int get hashCode =>
        window.hashCode +
        (capUsd == null ? 0 : capUsd.hashCode);

  factory TranslationSettingsInputBudget.fromJson(Map<String, dynamic> json) => _$TranslationSettingsInputBudgetFromJson(json);

  Map<String, dynamic> toJson() => _$TranslationSettingsInputBudgetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// The UTC window the cap applies to
enum TranslationSettingsInputBudgetWindowEnum {
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'month')
month(r'month');

const TranslationSettingsInputBudgetWindowEnum(this.value);

final String value;

@override
String toString() => value;
}


