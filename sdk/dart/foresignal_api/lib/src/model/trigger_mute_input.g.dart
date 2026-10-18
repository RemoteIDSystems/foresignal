// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_mute_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerMuteInputCWProxy {
  TriggerMuteInput until(DateTime until);

  TriggerMuteInput for_(String for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInput(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerMuteInput call({DateTime until, String for_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerMuteInput.copyWith(...)` or call `instanceOfTriggerMuteInput.copyWith.fieldName(value)` for a single field.
class _$TriggerMuteInputCWProxyImpl implements _$TriggerMuteInputCWProxy {
  const _$TriggerMuteInputCWProxyImpl(this._value);

  final TriggerMuteInput _value;

  @override
  TriggerMuteInput until(DateTime until) => call(until: until);

  @override
  TriggerMuteInput for_(String for_) => call(for_: for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerMuteInput call({
    Object? until = const $CopyWithPlaceholder(),
    Object? for_ = const $CopyWithPlaceholder(),
  }) {
    return TriggerMuteInput(
      until: until == const $CopyWithPlaceholder() || until == null
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime,
      for_: for_ == const $CopyWithPlaceholder() || for_ == null
          ? _value.for_
          // ignore: cast_nullable_to_non_nullable
          : for_ as String,
    );
  }
}

extension $TriggerMuteInputCopyWith on TriggerMuteInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerMuteInput.copyWith(...)` or `instanceOfTriggerMuteInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerMuteInputCWProxy get copyWith => _$TriggerMuteInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerMuteInput _$TriggerMuteInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TriggerMuteInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['until', 'for']);
      final val = TriggerMuteInput(
        until: $checkedConvert('until', (v) => DateTime.parse(v as String)),
        for_: $checkedConvert('for', (v) => v as String),
      );
      return val;
    }, fieldKeyMap: const {'for_': 'for'});

Map<String, dynamic> _$TriggerMuteInputToJson(TriggerMuteInput instance) =>
    <String, dynamic>{
      'until': instance.until.toIso8601String(),
      'for': instance.for_,
    };
