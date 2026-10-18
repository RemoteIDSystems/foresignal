// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'source_access.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SourceAccessCWProxy {
  SourceAccess route(SourceAccessRouteEnum route);

  SourceAccess termsSummary(String? termsSummary);

  SourceAccess termsUrl(String? termsUrl);

  SourceAccess fragility(SourceAccessFragilityEnum? fragility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceAccess(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceAccess(...).copyWith(id: 12, name: "My name")
  /// ```
  SourceAccess call({
    SourceAccessRouteEnum route,
    String? termsSummary,
    String? termsUrl,
    SourceAccessFragilityEnum? fragility,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSourceAccess.copyWith(...)` or call `instanceOfSourceAccess.copyWith.fieldName(value)` for a single field.
class _$SourceAccessCWProxyImpl implements _$SourceAccessCWProxy {
  const _$SourceAccessCWProxyImpl(this._value);

  final SourceAccess _value;

  @override
  SourceAccess route(SourceAccessRouteEnum route) => call(route: route);

  @override
  SourceAccess termsSummary(String? termsSummary) =>
      call(termsSummary: termsSummary);

  @override
  SourceAccess termsUrl(String? termsUrl) => call(termsUrl: termsUrl);

  @override
  SourceAccess fragility(SourceAccessFragilityEnum? fragility) =>
      call(fragility: fragility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SourceAccess(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SourceAccess(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SourceAccess call({
    Object? route = const $CopyWithPlaceholder(),
    Object? termsSummary = const $CopyWithPlaceholder(),
    Object? termsUrl = const $CopyWithPlaceholder(),
    Object? fragility = const $CopyWithPlaceholder(),
  }) {
    return SourceAccess(
      route: route == const $CopyWithPlaceholder() || route == null
          ? _value.route
          // ignore: cast_nullable_to_non_nullable
          : route as SourceAccessRouteEnum,
      termsSummary: termsSummary == const $CopyWithPlaceholder()
          ? _value.termsSummary
          // ignore: cast_nullable_to_non_nullable
          : termsSummary as String?,
      termsUrl: termsUrl == const $CopyWithPlaceholder()
          ? _value.termsUrl
          // ignore: cast_nullable_to_non_nullable
          : termsUrl as String?,
      fragility: fragility == const $CopyWithPlaceholder()
          ? _value.fragility
          // ignore: cast_nullable_to_non_nullable
          : fragility as SourceAccessFragilityEnum?,
    );
  }
}

extension $SourceAccessCopyWith on SourceAccess {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSourceAccess.copyWith(...)` or `instanceOfSourceAccess.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SourceAccessCWProxy get copyWith => _$SourceAccessCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SourceAccess _$SourceAccessFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SourceAccess', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['route']);
      final val = SourceAccess(
        route: $checkedConvert(
          'route',
          (v) => $enumDecode(_$SourceAccessRouteEnumEnumMap, v),
        ),
        termsSummary: $checkedConvert('termsSummary', (v) => v as String?),
        termsUrl: $checkedConvert('termsUrl', (v) => v as String?),
        fragility: $checkedConvert(
          'fragility',
          (v) => $enumDecodeNullable(_$SourceAccessFragilityEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SourceAccessToJson(SourceAccess instance) =>
    <String, dynamic>{
      'route': _$SourceAccessRouteEnumEnumMap[instance.route]!,
      'termsSummary': ?instance.termsSummary,
      'termsUrl': ?instance.termsUrl,
      'fragility': ?_$SourceAccessFragilityEnumEnumMap[instance.fragility],
    };

const _$SourceAccessRouteEnumEnumMap = {
  SourceAccessRouteEnum.official: 'official',
  SourceAccessRouteEnum.unofficialApi: 'unofficial-api',
  SourceAccessRouteEnum.scrape: 'scrape',
  SourceAccessRouteEnum.browser: 'browser',
  SourceAccessRouteEnum.other: 'other',
};

const _$SourceAccessFragilityEnumEnumMap = {
  SourceAccessFragilityEnum.low: 'low',
  SourceAccessFragilityEnum.medium: 'medium',
  SourceAccessFragilityEnum.high: 'high',
};
