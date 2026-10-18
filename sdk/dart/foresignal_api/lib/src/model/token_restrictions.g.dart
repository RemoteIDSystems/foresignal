// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'token_restrictions.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TokenRestrictionsCWProxy {
  TokenRestrictions sources(List<String>? sources);

  TokenRestrictions kinds(List<String>? kinds);

  TokenRestrictions bbox(List<num>? bbox);

  TokenRestrictions endpoints(List<String>? endpoints);

  TokenRestrictions federation(bool? federation);

  TokenRestrictions includeExercise(bool? includeExercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TokenRestrictions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TokenRestrictions(...).copyWith(id: 12, name: "My name")
  /// ```
  TokenRestrictions call({
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    List<String>? endpoints,
    bool? federation,
    bool? includeExercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTokenRestrictions.copyWith(...)` or call `instanceOfTokenRestrictions.copyWith.fieldName(value)` for a single field.
class _$TokenRestrictionsCWProxyImpl implements _$TokenRestrictionsCWProxy {
  const _$TokenRestrictionsCWProxyImpl(this._value);

  final TokenRestrictions _value;

  @override
  TokenRestrictions sources(List<String>? sources) => call(sources: sources);

  @override
  TokenRestrictions kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  TokenRestrictions bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  TokenRestrictions endpoints(List<String>? endpoints) =>
      call(endpoints: endpoints);

  @override
  TokenRestrictions federation(bool? federation) =>
      call(federation: federation);

  @override
  TokenRestrictions includeExercise(bool? includeExercise) =>
      call(includeExercise: includeExercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TokenRestrictions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TokenRestrictions(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TokenRestrictions call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? endpoints = const $CopyWithPlaceholder(),
    Object? federation = const $CopyWithPlaceholder(),
    Object? includeExercise = const $CopyWithPlaceholder(),
  }) {
    return TokenRestrictions(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      endpoints: endpoints == const $CopyWithPlaceholder()
          ? _value.endpoints
          // ignore: cast_nullable_to_non_nullable
          : endpoints as List<String>?,
      federation: federation == const $CopyWithPlaceholder()
          ? _value.federation
          // ignore: cast_nullable_to_non_nullable
          : federation as bool?,
      includeExercise: includeExercise == const $CopyWithPlaceholder()
          ? _value.includeExercise
          // ignore: cast_nullable_to_non_nullable
          : includeExercise as bool?,
    );
  }
}

extension $TokenRestrictionsCopyWith on TokenRestrictions {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTokenRestrictions.copyWith(...)` or `instanceOfTokenRestrictions.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TokenRestrictionsCWProxy get copyWith =>
      _$TokenRestrictionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TokenRestrictions _$TokenRestrictionsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TokenRestrictions', json, ($checkedConvert) {
      final val = TokenRestrictions(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        endpoints: $checkedConvert(
          'endpoints',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        federation: $checkedConvert('federation', (v) => v as bool? ?? false),
        includeExercise: $checkedConvert(
          'includeExercise',
          (v) => v as bool? ?? false,
        ),
      );
      return val;
    });

Map<String, dynamic> _$TokenRestrictionsToJson(TokenRestrictions instance) =>
    <String, dynamic>{
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'bbox': ?instance.bbox,
      'endpoints': ?instance.endpoints,
      'federation': ?instance.federation,
      'includeExercise': ?instance.includeExercise,
    };
