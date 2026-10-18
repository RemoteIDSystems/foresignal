// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_source_info_access.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSourceInfoAccessCWProxy {
  EdpSourceInfoAccess route(EdpSourceInfoAccessRouteEnum route);

  EdpSourceInfoAccess fragility(EdpSourceInfoAccessFragilityEnum? fragility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoAccess(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoAccess(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSourceInfoAccess call({
    EdpSourceInfoAccessRouteEnum route,
    EdpSourceInfoAccessFragilityEnum? fragility,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSourceInfoAccess.copyWith(...)` or call `instanceOfEdpSourceInfoAccess.copyWith.fieldName(value)` for a single field.
class _$EdpSourceInfoAccessCWProxyImpl implements _$EdpSourceInfoAccessCWProxy {
  const _$EdpSourceInfoAccessCWProxyImpl(this._value);

  final EdpSourceInfoAccess _value;

  @override
  EdpSourceInfoAccess route(EdpSourceInfoAccessRouteEnum route) =>
      call(route: route);

  @override
  EdpSourceInfoAccess fragility(EdpSourceInfoAccessFragilityEnum? fragility) =>
      call(fragility: fragility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSourceInfoAccess(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSourceInfoAccess(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSourceInfoAccess call({
    Object? route = const $CopyWithPlaceholder(),
    Object? fragility = const $CopyWithPlaceholder(),
  }) {
    return EdpSourceInfoAccess(
      route: route == const $CopyWithPlaceholder() || route == null
          ? _value.route
          // ignore: cast_nullable_to_non_nullable
          : route as EdpSourceInfoAccessRouteEnum,
      fragility: fragility == const $CopyWithPlaceholder()
          ? _value.fragility
          // ignore: cast_nullable_to_non_nullable
          : fragility as EdpSourceInfoAccessFragilityEnum?,
    );
  }
}

extension $EdpSourceInfoAccessCopyWith on EdpSourceInfoAccess {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSourceInfoAccess.copyWith(...)` or `instanceOfEdpSourceInfoAccess.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSourceInfoAccessCWProxy get copyWith =>
      _$EdpSourceInfoAccessCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSourceInfoAccess _$EdpSourceInfoAccessFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSourceInfoAccess', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['route']);
      final val = EdpSourceInfoAccess(
        route: $checkedConvert(
          'route',
          (v) => $enumDecode(_$EdpSourceInfoAccessRouteEnumEnumMap, v),
        ),
        fragility: $checkedConvert(
          'fragility',
          (v) =>
              $enumDecodeNullable(_$EdpSourceInfoAccessFragilityEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpSourceInfoAccessToJson(
  EdpSourceInfoAccess instance,
) => <String, dynamic>{
  'route': _$EdpSourceInfoAccessRouteEnumEnumMap[instance.route]!,
  'fragility': ?_$EdpSourceInfoAccessFragilityEnumEnumMap[instance.fragility],
};

const _$EdpSourceInfoAccessRouteEnumEnumMap = {
  EdpSourceInfoAccessRouteEnum.official: 'official',
  EdpSourceInfoAccessRouteEnum.unofficialApi: 'unofficial-api',
  EdpSourceInfoAccessRouteEnum.scrape: 'scrape',
  EdpSourceInfoAccessRouteEnum.browser: 'browser',
  EdpSourceInfoAccessRouteEnum.other: 'other',
};

const _$EdpSourceInfoAccessFragilityEnumEnumMap = {
  EdpSourceInfoAccessFragilityEnum.low: 'low',
  EdpSourceInfoAccessFragilityEnum.medium: 'medium',
  EdpSourceInfoAccessFragilityEnum.high: 'high',
};
