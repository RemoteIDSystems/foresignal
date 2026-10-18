// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'discoverability.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DiscoverabilityCWProxy {
  Discoverability discoverability(
    DiscoverabilityDiscoverabilityEnum discoverability,
  );

  Discoverability revealTitle(bool? revealTitle);

  Discoverability revealOwner(bool? revealOwner);

  Discoverability maxAllowed(DiscoverabilityMaxAllowedEnum? maxAllowed);

  Discoverability tlp(Tlp? tlp);

  Discoverability updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Discoverability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Discoverability(...).copyWith(id: 12, name: "My name")
  /// ```
  Discoverability call({
    DiscoverabilityDiscoverabilityEnum discoverability,
    bool? revealTitle,
    bool? revealOwner,
    DiscoverabilityMaxAllowedEnum? maxAllowed,
    Tlp? tlp,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDiscoverability.copyWith(...)` or call `instanceOfDiscoverability.copyWith.fieldName(value)` for a single field.
class _$DiscoverabilityCWProxyImpl implements _$DiscoverabilityCWProxy {
  const _$DiscoverabilityCWProxyImpl(this._value);

  final Discoverability _value;

  @override
  Discoverability discoverability(
    DiscoverabilityDiscoverabilityEnum discoverability,
  ) => call(discoverability: discoverability);

  @override
  Discoverability revealTitle(bool? revealTitle) =>
      call(revealTitle: revealTitle);

  @override
  Discoverability revealOwner(bool? revealOwner) =>
      call(revealOwner: revealOwner);

  @override
  Discoverability maxAllowed(DiscoverabilityMaxAllowedEnum? maxAllowed) =>
      call(maxAllowed: maxAllowed);

  @override
  Discoverability tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Discoverability updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Discoverability(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Discoverability(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Discoverability call({
    Object? discoverability = const $CopyWithPlaceholder(),
    Object? revealTitle = const $CopyWithPlaceholder(),
    Object? revealOwner = const $CopyWithPlaceholder(),
    Object? maxAllowed = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Discoverability(
      discoverability:
          discoverability == const $CopyWithPlaceholder() ||
              discoverability == null
          ? _value.discoverability
          // ignore: cast_nullable_to_non_nullable
          : discoverability as DiscoverabilityDiscoverabilityEnum,
      revealTitle: revealTitle == const $CopyWithPlaceholder()
          ? _value.revealTitle
          // ignore: cast_nullable_to_non_nullable
          : revealTitle as bool?,
      revealOwner: revealOwner == const $CopyWithPlaceholder()
          ? _value.revealOwner
          // ignore: cast_nullable_to_non_nullable
          : revealOwner as bool?,
      maxAllowed: maxAllowed == const $CopyWithPlaceholder()
          ? _value.maxAllowed
          // ignore: cast_nullable_to_non_nullable
          : maxAllowed as DiscoverabilityMaxAllowedEnum?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $DiscoverabilityCopyWith on Discoverability {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDiscoverability.copyWith(...)` or `instanceOfDiscoverability.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DiscoverabilityCWProxy get copyWith => _$DiscoverabilityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Discoverability _$DiscoverabilityFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Discoverability', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['discoverability']);
      final val = Discoverability(
        discoverability: $checkedConvert(
          'discoverability',
          (v) => $enumDecode(_$DiscoverabilityDiscoverabilityEnumEnumMap, v),
        ),
        revealTitle: $checkedConvert('revealTitle', (v) => v as bool? ?? false),
        revealOwner: $checkedConvert('revealOwner', (v) => v as bool? ?? false),
        maxAllowed: $checkedConvert(
          'maxAllowed',
          (v) => $enumDecodeNullable(_$DiscoverabilityMaxAllowedEnumEnumMap, v),
        ),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DiscoverabilityToJson(
  Discoverability instance,
) => <String, dynamic>{
  'discoverability':
      _$DiscoverabilityDiscoverabilityEnumEnumMap[instance.discoverability]!,
  'revealTitle': ?instance.revealTitle,
  'revealOwner': ?instance.revealOwner,
  'maxAllowed': ?_$DiscoverabilityMaxAllowedEnumEnumMap[instance.maxAllowed],
  'tlp': ?_$TlpEnumMap[instance.tlp],
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$DiscoverabilityDiscoverabilityEnumEnumMap = {
  DiscoverabilityDiscoverabilityEnum.none: 'none',
  DiscoverabilityDiscoverabilityEnum.instance: 'instance',
  DiscoverabilityDiscoverabilityEnum.network: 'network',
};

const _$DiscoverabilityMaxAllowedEnumEnumMap = {
  DiscoverabilityMaxAllowedEnum.none: 'none',
  DiscoverabilityMaxAllowedEnum.instance: 'instance',
  DiscoverabilityMaxAllowedEnum.network: 'network',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
