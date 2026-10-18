// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tuning_suggestion_apply.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TuningSuggestionApplyCWProxy {
  TuningSuggestionApply note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionApply(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionApply(...).copyWith(id: 12, name: "My name")
  /// ```
  TuningSuggestionApply call({String? note});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTuningSuggestionApply.copyWith(...)` or call `instanceOfTuningSuggestionApply.copyWith.fieldName(value)` for a single field.
class _$TuningSuggestionApplyCWProxyImpl
    implements _$TuningSuggestionApplyCWProxy {
  const _$TuningSuggestionApplyCWProxyImpl(this._value);

  final TuningSuggestionApply _value;

  @override
  TuningSuggestionApply note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TuningSuggestionApply(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TuningSuggestionApply(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TuningSuggestionApply call({Object? note = const $CopyWithPlaceholder()}) {
    return TuningSuggestionApply(
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $TuningSuggestionApplyCopyWith on TuningSuggestionApply {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTuningSuggestionApply.copyWith(...)` or `instanceOfTuningSuggestionApply.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TuningSuggestionApplyCWProxy get copyWith =>
      _$TuningSuggestionApplyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TuningSuggestionApply _$TuningSuggestionApplyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TuningSuggestionApply', json, ($checkedConvert) {
  final val = TuningSuggestionApply(
    note: $checkedConvert('note', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$TuningSuggestionApplyToJson(
  TuningSuggestionApply instance,
) => <String, dynamic>{'note': ?instance.note};
