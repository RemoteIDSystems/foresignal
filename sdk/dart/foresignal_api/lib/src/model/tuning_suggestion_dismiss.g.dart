// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion_dismiss.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionDismissCWProxy {
  TuningSuggestionDismiss reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionDismiss(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionDismiss(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestionDismiss call({String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestionDismiss.copyWith(...)` or call `instanceOfTuningSuggestionDismiss.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionDismissCWProxyImpl
    implements _$TuningSuggestionDismissCWProxy {
  const _$TuningSuggestionDismissCWProxyImpl(this._value);

  final TuningSuggestionDismiss _value;

  @override
  TuningSuggestionDismiss reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionDismiss(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionDismiss(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestionDismiss call({
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return TuningSuggestionDismiss(
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $TuningSuggestionDismissCopyWith on TuningSuggestionDismiss {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestionDismiss.copyWith(...)` or `instanceOfTuningSuggestionDismiss.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionDismissCWProxy get copyWith =>
      _$TuningSuggestionDismissCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestionDismiss _$TuningSuggestionDismissFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestionDismiss', json, ($checkedConvert) {
  final val = TuningSuggestionDismiss(
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$TuningSuggestionDismissToJson(
  TuningSuggestionDismiss instance,
) => <String, dynamic>{'reason': ?instance.reason};
