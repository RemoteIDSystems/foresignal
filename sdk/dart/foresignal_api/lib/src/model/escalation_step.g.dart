// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation_step.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationStepCWProxy {
  EscalationStep afterMinutes(int afterMinutes);

  EscalationStep channelIds(List<String>? channelIds);

  EscalationStep includePush(bool? includePush);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationStep(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationStep(...).copyWith(id: 12, name: "My name")
  /// ```
  EscalationStep call({
    int afterMinutes,
    List<String>? channelIds,
    bool? includePush,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalationStep.copyWith(...)` or call `instanceOfEscalationStep.copyWith.fieldName(value)` for a single field.
class _$EscalationStepCWProxyImpl implements _$EscalationStepCWProxy {
  const _$EscalationStepCWProxyImpl(this._value);

  final EscalationStep _value;

  @override
  EscalationStep afterMinutes(int afterMinutes) =>
      call(afterMinutes: afterMinutes);

  @override
  EscalationStep channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  EscalationStep includePush(bool? includePush) =>
      call(includePush: includePush);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationStep(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationStep(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EscalationStep call({
    Object? afterMinutes = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? includePush = const $CopyWithPlaceholder(),
  }) {
    return EscalationStep(
      afterMinutes:
          afterMinutes == const $CopyWithPlaceholder() || afterMinutes == null
          ? _value.afterMinutes
          // ignore: cast_nullable_to_non_nullable
          : afterMinutes as int,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      includePush: includePush == const $CopyWithPlaceholder()
          ? _value.includePush
          // ignore: cast_nullable_to_non_nullable
          : includePush as bool?,
    );
  }
}

extension $EscalationStepCopyWith on EscalationStep {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalationStep.copyWith(...)` or `instanceOfEscalationStep.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationStepCWProxy get copyWith => _$EscalationStepCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EscalationStep _$EscalationStepFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EscalationStep', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['afterMinutes']);
      final val = EscalationStep(
        afterMinutes: $checkedConvert(
          'afterMinutes',
          (v) => (v as num).toInt(),
        ),
        channelIds: $checkedConvert(
          'channelIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        includePush: $checkedConvert('includePush', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$EscalationStepToJson(EscalationStep instance) =>
    <String, dynamic>{
      'afterMinutes': instance.afterMinutes,
      'channelIds': ?instance.channelIds,
      'includePush': ?instance.includePush,
    };
