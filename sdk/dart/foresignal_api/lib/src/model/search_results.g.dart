// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'search_results.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SearchResultsCWProxy {
  SearchResults mode(SearchResultsModeEnum mode);

  SearchResults items(List<SearchResult> items);

  SearchResults degraded(bool? degraded);

  SearchResults attributions(List<Attribution>? attributions);

  SearchResults licenceFiltered(int? licenceFiltered);

  SearchResults reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchResults(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchResults(...).copyWith(id: 12, name: "My name")
  /// ```
  SearchResults call({
    SearchResultsModeEnum mode,
    List<SearchResult> items,
    bool? degraded,
    List<Attribution>? attributions,
    int? licenceFiltered,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSearchResults.copyWith(...)` or call `instanceOfSearchResults.copyWith.fieldName(value)` for a single field.
class _$SearchResultsCWProxyImpl implements _$SearchResultsCWProxy {
  const _$SearchResultsCWProxyImpl(this._value);

  final SearchResults _value;

  @override
  SearchResults mode(SearchResultsModeEnum mode) => call(mode: mode);

  @override
  SearchResults items(List<SearchResult> items) => call(items: items);

  @override
  SearchResults degraded(bool? degraded) => call(degraded: degraded);

  @override
  SearchResults attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  SearchResults licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  @override
  SearchResults reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SearchResults(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SearchResults(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SearchResults call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? degraded = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return SearchResults(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as SearchResultsModeEnum,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SearchResult>,
      degraded: degraded == const $CopyWithPlaceholder()
          ? _value.degraded
          // ignore: cast_nullable_to_non_nullable
          : degraded as bool?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $SearchResultsCopyWith on SearchResults {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSearchResults.copyWith(...)` or `instanceOfSearchResults.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SearchResultsCWProxy get copyWith => _$SearchResultsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SearchResults _$SearchResultsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SearchResults', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mode', 'items']);
      final val = SearchResults(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$SearchResultsModeEnumEnumMap, v),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SearchResult.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        degraded: $checkedConvert('degraded', (v) => v as bool?),
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        licenceFiltered: $checkedConvert(
          'licenceFiltered',
          (v) => (v as num?)?.toInt(),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SearchResultsToJson(SearchResults instance) =>
    <String, dynamic>{
      'mode': _$SearchResultsModeEnumEnumMap[instance.mode]!,
      'items': instance.items.map((e) => e.toJson()).toList(),
      'degraded': ?instance.degraded,
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'licenceFiltered': ?instance.licenceFiltered,
      'reason': ?instance.reason,
    };

const _$SearchResultsModeEnumEnumMap = {
  SearchResultsModeEnum.hybrid: 'hybrid',
  SearchResultsModeEnum.semantic: 'semantic',
  SearchResultsModeEnum.text: 'text',
};
