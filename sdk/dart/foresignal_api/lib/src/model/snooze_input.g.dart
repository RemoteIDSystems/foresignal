// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'snooze_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SnoozeInputCWProxy {
  SnoozeInput until(DateTime? until);

  SnoozeInput for_(String? for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SnoozeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SnoozeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SnoozeInput call({DateTime? until, String? for_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSnoozeInput.copyWith(...)` or call `instanceOfSnoozeInput.copyWith.fieldName(value)` for a single field.
class _$SnoozeInputCWProxyImpl implements _$SnoozeInputCWProxy {
  const _$SnoozeInputCWProxyImpl(this._value);

  final SnoozeInput _value;

  @override
  SnoozeInput until(DateTime? until) => call(until: until);

  @override
  SnoozeInput for_(String? for_) => call(for_: for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SnoozeInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SnoozeInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SnoozeInput call({
    Object? until = const $CopyWithPlaceholder(),
    Object? for_ = const $CopyWithPlaceholder(),
  }) {
    return SnoozeInput(
      until: until == const $CopyWithPlaceholder()
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime?,
      for_: for_ == const $CopyWithPlaceholder()
          ? _value.for_
          // ignore: cast_nullable_to_non_nullable
          : for_ as String?,
    );
  }
}

extension $SnoozeInputCopyWith on SnoozeInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSnoozeInput.copyWith(...)` or `instanceOfSnoozeInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SnoozeInputCWProxy get copyWith => _$SnoozeInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SnoozeInput _$SnoozeInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SnoozeInput', json, ($checkedConvert) {
      final val = SnoozeInput(
        until: $checkedConvert(
          'until',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        for_: $checkedConvert('for', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'for_': 'for'});

Map<String, dynamic> _$SnoozeInputToJson(SnoozeInput instance) =>
    <String, dynamic>{
      'until': ?instance.until?.toIso8601String(),
      'for': ?instance.for_,
    };
