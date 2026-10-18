// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'action_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ActionResultCWProxy {
  ActionResult action(ActionResultActionEnum action);

  ActionResult alertId(int alertId);

  ActionResult result(String result);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActionResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActionResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ActionResult call({
    ActionResultActionEnum action,
    int alertId,
    String result,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfActionResult.copyWith(...)` or call `instanceOfActionResult.copyWith.fieldName(value)` for a single field.
class _$ActionResultCWProxyImpl implements _$ActionResultCWProxy {
  const _$ActionResultCWProxyImpl(this._value);

  final ActionResult _value;

  @override
  ActionResult action(ActionResultActionEnum action) => call(action: action);

  @override
  ActionResult alertId(int alertId) => call(alertId: alertId);

  @override
  ActionResult result(String result) => call(result: result);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActionResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActionResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ActionResult call({
    Object? action = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? result = const $CopyWithPlaceholder(),
  }) {
    return ActionResult(
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as ActionResultActionEnum,
      alertId: alertId == const $CopyWithPlaceholder() || alertId == null
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int,
      result: result == const $CopyWithPlaceholder() || result == null
          ? _value.result
          // ignore: cast_nullable_to_non_nullable
          : result as String,
    );
  }
}

extension $ActionResultCopyWith on ActionResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfActionResult.copyWith(...)` or `instanceOfActionResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ActionResultCWProxy get copyWith => _$ActionResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ActionResult _$ActionResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ActionResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['action', 'alertId', 'result']);
      final val = ActionResult(
        action: $checkedConvert(
          'action',
          (v) => $enumDecode(_$ActionResultActionEnumEnumMap, v),
        ),
        alertId: $checkedConvert('alertId', (v) => (v as num).toInt()),
        result: $checkedConvert('result', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ActionResultToJson(ActionResult instance) =>
    <String, dynamic>{
      'action': _$ActionResultActionEnumEnumMap[instance.action]!,
      'alertId': instance.alertId,
      'result': instance.result,
    };

const _$ActionResultActionEnumEnumMap = {
  ActionResultActionEnum.ack: 'ack',
  ActionResultActionEnum.snooze: 'snooze',
  ActionResultActionEnum.mute: 'mute',
  ActionResultActionEnum.feedbackColonUseful: 'feedback:useful',
  ActionResultActionEnum.feedbackColonNoise: 'feedback:noise',
};
