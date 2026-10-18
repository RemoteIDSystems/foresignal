// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleScopeCWProxy {
  CycleScope regionCodes(List<String>? regionCodes);

  CycleScope themes(List<String>? themes);

  CycleScope situationIds(List<String>? situationIds);

  CycleScope hypothesisSetIds(Object? hypothesisSetIds);

  CycleScope sourceIds(List<String>? sourceIds);

  CycleScope categories(List<String>? categories);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleScope(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleScope call({
    List<String>? regionCodes,
    List<String>? themes,
    List<String>? situationIds,
    Object? hypothesisSetIds,
    List<String>? sourceIds,
    List<String>? categories,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleScope.copyWith(...)` or call `instanceOfCycleScope.copyWith.fieldName(value)` for a single field.
class _$CycleScopeCWProxyImpl implements _$CycleScopeCWProxy {
  const _$CycleScopeCWProxyImpl(this._value);

  final CycleScope _value;

  @override
  CycleScope regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  CycleScope themes(List<String>? themes) => call(themes: themes);

  @override
  CycleScope situationIds(List<String>? situationIds) =>
      call(situationIds: situationIds);

  @override
  CycleScope hypothesisSetIds(Object? hypothesisSetIds) =>
      call(hypothesisSetIds: hypothesisSetIds);

  @override
  CycleScope sourceIds(List<String>? sourceIds) => call(sourceIds: sourceIds);

  @override
  CycleScope categories(List<String>? categories) =>
      call(categories: categories);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleScope(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleScope(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleScope call({
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? themes = const $CopyWithPlaceholder(),
    Object? situationIds = const $CopyWithPlaceholder(),
    Object? hypothesisSetIds = const $CopyWithPlaceholder(),
    Object? sourceIds = const $CopyWithPlaceholder(),
    Object? categories = const $CopyWithPlaceholder(),
  }) {
    return CycleScope(
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      themes: themes == const $CopyWithPlaceholder()
          ? _value.themes
          // ignore: cast_nullable_to_non_nullable
          : themes as List<String>?,
      situationIds: situationIds == const $CopyWithPlaceholder()
          ? _value.situationIds
          // ignore: cast_nullable_to_non_nullable
          : situationIds as List<String>?,
      hypothesisSetIds: hypothesisSetIds == const $CopyWithPlaceholder()
          ? _value.hypothesisSetIds
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetIds as Object?,
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
      categories: categories == const $CopyWithPlaceholder()
          ? _value.categories
          // ignore: cast_nullable_to_non_nullable
          : categories as List<String>?,
    );
  }
}

extension $CycleScopeCopyWith on CycleScope {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleScope.copyWith(...)` or `instanceOfCycleScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleScopeCWProxy get copyWith => _$CycleScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleScope _$CycleScopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CycleScope', json, ($checkedConvert) {
      final val = CycleScope(
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        themes: $checkedConvert(
          'themes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        situationIds: $checkedConvert(
          'situationIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        hypothesisSetIds: $checkedConvert('hypothesisSetIds', (v) => v),
        sourceIds: $checkedConvert(
          'sourceIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        categories: $checkedConvert(
          'categories',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CycleScopeToJson(CycleScope instance) =>
    <String, dynamic>{
      'regionCodes': ?instance.regionCodes,
      'themes': ?instance.themes,
      'situationIds': ?instance.situationIds,
      'hypothesisSetIds': ?instance.hypothesisSetIds,
      'sourceIds': ?instance.sourceIds,
      'categories': ?instance.categories,
    };
