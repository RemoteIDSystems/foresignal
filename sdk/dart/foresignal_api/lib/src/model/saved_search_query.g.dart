// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_query.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchQueryCWProxy {
  SavedSearchQuery types(List<String>? types);

  SavedSearchQuery text(String? text);

  SavedSearchQuery filters(Map<String, Object>? filters);

  SavedSearchQuery area(SavedSearchQueryArea? area);

  SavedSearchQuery timeWindow(SavedSearchQueryTimeWindow? timeWindow);

  SavedSearchQuery tags(List<String>? tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchQuery call({
    List<String>? types,
    String? text,
    Map<String, Object>? filters,
    SavedSearchQueryArea? area,
    SavedSearchQueryTimeWindow? timeWindow,
    List<String>? tags,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchQuery.copyWith(...)` or call `instanceOfSavedSearchQuery.copyWith.fieldName(value)` for a single field.
class _$SavedSearchQueryCWProxyImpl implements _$SavedSearchQueryCWProxy {
  const _$SavedSearchQueryCWProxyImpl(this._value);

  final SavedSearchQuery _value;

  @override
  SavedSearchQuery types(List<String>? types) => call(types: types);

  @override
  SavedSearchQuery text(String? text) => call(text: text);

  @override
  SavedSearchQuery filters(Map<String, Object>? filters) =>
      call(filters: filters);

  @override
  SavedSearchQuery area(SavedSearchQueryArea? area) => call(area: area);

  @override
  SavedSearchQuery timeWindow(SavedSearchQueryTimeWindow? timeWindow) =>
      call(timeWindow: timeWindow);

  @override
  SavedSearchQuery tags(List<String>? tags) => call(tags: tags);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchQuery call({
    Object? types = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? timeWindow = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchQuery(
      types: types == const $CopyWithPlaceholder()
          ? _value.types
          // ignore: cast_nullable_to_non_nullable
          : types as List<String>?,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as Map<String, Object>?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as SavedSearchQueryArea?,
      timeWindow: timeWindow == const $CopyWithPlaceholder()
          ? _value.timeWindow
          // ignore: cast_nullable_to_non_nullable
          : timeWindow as SavedSearchQueryTimeWindow?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
    );
  }
}

extension $SavedSearchQueryCopyWith on SavedSearchQuery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchQuery.copyWith(...)` or `instanceOfSavedSearchQuery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchQueryCWProxy get copyWith => _$SavedSearchQueryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchQuery _$SavedSearchQueryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearchQuery', json, ($checkedConvert) {
      final val = SavedSearchQuery(
        types: $checkedConvert(
          'types',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        text: $checkedConvert('text', (v) => v as String?),
        filters: $checkedConvert(
          'filters',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        area: $checkedConvert(
          'area',
          (v) => v == null
              ? null
              : SavedSearchQueryArea.fromJson(v as Map<String, dynamic>),
        ),
        timeWindow: $checkedConvert(
          'timeWindow',
          (v) => v == null
              ? null
              : SavedSearchQueryTimeWindow.fromJson(v as Map<String, dynamic>),
        ),
        tags: $checkedConvert(
          'tags',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchQueryToJson(SavedSearchQuery instance) =>
    <String, dynamic>{
      'types': ?instance.types,
      'text': ?instance.text,
      'filters': ?instance.filters,
      'area': ?instance.area?.toJson(),
      'timeWindow': ?instance.timeWindow?.toJson(),
      'tags': ?instance.tags,
    };
