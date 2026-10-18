// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'outlet_consensus_providers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OutletConsensusProvidersInnerCWProxy {
  OutletConsensusProvidersInner provider(
    OutletConsensusProvidersInnerProviderEnum provider,
  );

  OutletConsensusProvidersInner valueNorm(num? valueNorm);

  OutletConsensusProvidersInner label(String? label);

  OutletConsensusProvidersInner ratedAt(DateTime? ratedAt);

  OutletConsensusProvidersInner licence(String? licence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutletConsensusProvidersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutletConsensusProvidersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OutletConsensusProvidersInner call({
    OutletConsensusProvidersInnerProviderEnum provider,
    num? valueNorm,
    String? label,
    DateTime? ratedAt,
    String? licence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOutletConsensusProvidersInner.copyWith(...)` or call `instanceOfOutletConsensusProvidersInner.copyWith.fieldName(value)` for a single field.
class _$OutletConsensusProvidersInnerCWProxyImpl
    implements _$OutletConsensusProvidersInnerCWProxy {
  const _$OutletConsensusProvidersInnerCWProxyImpl(this._value);

  final OutletConsensusProvidersInner _value;

  @override
  OutletConsensusProvidersInner provider(
    OutletConsensusProvidersInnerProviderEnum provider,
  ) => call(provider: provider);

  @override
  OutletConsensusProvidersInner valueNorm(num? valueNorm) =>
      call(valueNorm: valueNorm);

  @override
  OutletConsensusProvidersInner label(String? label) => call(label: label);

  @override
  OutletConsensusProvidersInner ratedAt(DateTime? ratedAt) =>
      call(ratedAt: ratedAt);

  @override
  OutletConsensusProvidersInner licence(String? licence) =>
      call(licence: licence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OutletConsensusProvidersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OutletConsensusProvidersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OutletConsensusProvidersInner call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? valueNorm = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? ratedAt = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
  }) {
    return OutletConsensusProvidersInner(
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as OutletConsensusProvidersInnerProviderEnum,
      valueNorm: valueNorm == const $CopyWithPlaceholder()
          ? _value.valueNorm
          // ignore: cast_nullable_to_non_nullable
          : valueNorm as num?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      ratedAt: ratedAt == const $CopyWithPlaceholder()
          ? _value.ratedAt
          // ignore: cast_nullable_to_non_nullable
          : ratedAt as DateTime?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
    );
  }
}

extension $OutletConsensusProvidersInnerCopyWith
    on OutletConsensusProvidersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOutletConsensusProvidersInner.copyWith(...)` or `instanceOfOutletConsensusProvidersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OutletConsensusProvidersInnerCWProxy get copyWith =>
      _$OutletConsensusProvidersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OutletConsensusProvidersInner _$OutletConsensusProvidersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OutletConsensusProvidersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['provider']);
  final val = OutletConsensusProvidersInner(
    provider: $checkedConvert(
      'provider',
      (v) => $enumDecode(_$OutletConsensusProvidersInnerProviderEnumEnumMap, v),
    ),
    valueNorm: $checkedConvert('valueNorm', (v) => v as num?),
    label: $checkedConvert('label', (v) => v as String?),
    ratedAt: $checkedConvert(
      'ratedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    licence: $checkedConvert('licence', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$OutletConsensusProvidersInnerToJson(
  OutletConsensusProvidersInner instance,
) => <String, dynamic>{
  'provider':
      _$OutletConsensusProvidersInnerProviderEnumEnumMap[instance.provider]!,
  'valueNorm': ?instance.valueNorm,
  'label': ?instance.label,
  'ratedAt': ?instance.ratedAt?.toIso8601String(),
  'licence': ?instance.licence,
};

const _$OutletConsensusProvidersInnerProviderEnumEnumMap = {
  OutletConsensusProvidersInnerProviderEnum.wikidata: 'wikidata',
  OutletConsensusProvidersInnerProviderEnum.partisanWeb: 'partisan-web',
  OutletConsensusProvidersInnerProviderEnum.allsides: 'allsides',
  OutletConsensusProvidersInnerProviderEnum.mbfc: 'mbfc',
  OutletConsensusProvidersInnerProviderEnum.operator_: 'operator',
};
