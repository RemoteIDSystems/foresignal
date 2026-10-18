// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_query_time_window.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchQueryTimeWindowCWProxy {
  SavedSearchQueryTimeWindow start(DateTime? start);

  SavedSearchQueryTimeWindow end(DateTime? end);

  SavedSearchQueryTimeWindow relative(String? relative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQueryTimeWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQueryTimeWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchQueryTimeWindow call({
    DateTime? start,
    DateTime? end,
    String? relative,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchQueryTimeWindow.copyWith(...)` or call `instanceOfSavedSearchQueryTimeWindow.copyWith.fieldName(value)` for a single field.
class _$SavedSearchQueryTimeWindowCWProxyImpl
    implements _$SavedSearchQueryTimeWindowCWProxy {
  const _$SavedSearchQueryTimeWindowCWProxyImpl(this._value);

  final SavedSearchQueryTimeWindow _value;

  @override
  SavedSearchQueryTimeWindow start(DateTime? start) => call(start: start);

  @override
  SavedSearchQueryTimeWindow end(DateTime? end) => call(end: end);

  @override
  SavedSearchQueryTimeWindow relative(String? relative) =>
      call(relative: relative);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQueryTimeWindow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQueryTimeWindow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchQueryTimeWindow call({
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
    Object? relative = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchQueryTimeWindow(
      start: start == const $CopyWithPlaceholder()
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime?,
      end: end == const $CopyWithPlaceholder()
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as DateTime?,
      relative: relative == const $CopyWithPlaceholder()
          ? _value.relative
          // ignore: cast_nullable_to_non_nullable
          : relative as String?,
    );
  }
}

extension $SavedSearchQueryTimeWindowCopyWith on SavedSearchQueryTimeWindow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchQueryTimeWindow.copyWith(...)` or `instanceOfSavedSearchQueryTimeWindow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchQueryTimeWindowCWProxy get copyWith =>
      _$SavedSearchQueryTimeWindowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchQueryTimeWindow _$SavedSearchQueryTimeWindowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SavedSearchQueryTimeWindow', json, ($checkedConvert) {
  final val = SavedSearchQueryTimeWindow(
    start: $checkedConvert(
      'start',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    end: $checkedConvert(
      'end',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    relative: $checkedConvert('relative', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SavedSearchQueryTimeWindowToJson(
  SavedSearchQueryTimeWindow instance,
) => <String, dynamic>{
  'start': ?instance.start?.toIso8601String(),
  'end': ?instance.end?.toIso8601String(),
  'relative': ?instance.relative,
};
