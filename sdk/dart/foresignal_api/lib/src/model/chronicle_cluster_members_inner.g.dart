// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_cluster_members_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleClusterMembersInnerCWProxy {
  ChronicleClusterMembersInner disclosure(
    ChronicleClusterMembersInnerDisclosureEnum disclosure,
  );

  ChronicleClusterMembersInner chronicleId(String? chronicleId);

  ChronicleClusterMembersInner envelope(ObjectEnvelope? envelope);

  ChronicleClusterMembersInner linkId(String? linkId);

  ChronicleClusterMembersInner strength(
    ChronicleClusterMembersInnerStrengthEnum? strength,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleClusterMembersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleClusterMembersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleClusterMembersInner call({
    ChronicleClusterMembersInnerDisclosureEnum disclosure,
    String? chronicleId,
    ObjectEnvelope? envelope,
    String? linkId,
    ChronicleClusterMembersInnerStrengthEnum? strength,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleClusterMembersInner.copyWith(...)` or call `instanceOfChronicleClusterMembersInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleClusterMembersInnerCWProxyImpl
    implements _$ChronicleClusterMembersInnerCWProxy {
  const _$ChronicleClusterMembersInnerCWProxyImpl(this._value);

  final ChronicleClusterMembersInner _value;

  @override
  ChronicleClusterMembersInner disclosure(
    ChronicleClusterMembersInnerDisclosureEnum disclosure,
  ) => call(disclosure: disclosure);

  @override
  ChronicleClusterMembersInner chronicleId(String? chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleClusterMembersInner envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  @override
  ChronicleClusterMembersInner linkId(String? linkId) => call(linkId: linkId);

  @override
  ChronicleClusterMembersInner strength(
    ChronicleClusterMembersInnerStrengthEnum? strength,
  ) => call(strength: strength);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleClusterMembersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleClusterMembersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleClusterMembersInner call({
    Object? disclosure = const $CopyWithPlaceholder(),
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? linkId = const $CopyWithPlaceholder(),
    Object? strength = const $CopyWithPlaceholder(),
  }) {
    return ChronicleClusterMembersInner(
      disclosure:
          disclosure == const $CopyWithPlaceholder() || disclosure == null
          ? _value.disclosure
          // ignore: cast_nullable_to_non_nullable
          : disclosure as ChronicleClusterMembersInnerDisclosureEnum,
      chronicleId: chronicleId == const $CopyWithPlaceholder()
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String?,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      linkId: linkId == const $CopyWithPlaceholder()
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String?,
      strength: strength == const $CopyWithPlaceholder()
          ? _value.strength
          // ignore: cast_nullable_to_non_nullable
          : strength as ChronicleClusterMembersInnerStrengthEnum?,
    );
  }
}

extension $ChronicleClusterMembersInnerCopyWith
    on ChronicleClusterMembersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleClusterMembersInner.copyWith(...)` or `instanceOfChronicleClusterMembersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleClusterMembersInnerCWProxy get copyWith =>
      _$ChronicleClusterMembersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleClusterMembersInner _$ChronicleClusterMembersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleClusterMembersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['disclosure']);
  final val = ChronicleClusterMembersInner(
    disclosure: $checkedConvert(
      'disclosure',
      (v) =>
          $enumDecode(_$ChronicleClusterMembersInnerDisclosureEnumEnumMap, v),
    ),
    chronicleId: $checkedConvert('chronicleId', (v) => v as String?),
    envelope: $checkedConvert(
      'envelope',
      (v) =>
          v == null ? null : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    linkId: $checkedConvert('linkId', (v) => v as String?),
    strength: $checkedConvert(
      'strength',
      (v) => $enumDecodeNullable(
        _$ChronicleClusterMembersInnerStrengthEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleClusterMembersInnerToJson(
  ChronicleClusterMembersInner instance,
) => <String, dynamic>{
  'disclosure':
      _$ChronicleClusterMembersInnerDisclosureEnumEnumMap[instance.disclosure]!,
  'chronicleId': ?instance.chronicleId,
  'envelope': ?instance.envelope?.toJson(),
  'linkId': ?instance.linkId,
  'strength':
      ?_$ChronicleClusterMembersInnerStrengthEnumEnumMap[instance.strength],
};

const _$ChronicleClusterMembersInnerDisclosureEnumEnumMap = {
  ChronicleClusterMembersInnerDisclosureEnum.full: 'full',
  ChronicleClusterMembersInnerDisclosureEnum.opaque: 'opaque',
};

const _$ChronicleClusterMembersInnerStrengthEnumEnumMap = {
  ChronicleClusterMembersInnerStrengthEnum.low: 'low',
  ChronicleClusterMembersInnerStrengthEnum.medium: 'medium',
  ChronicleClusterMembersInnerStrengthEnum.high: 'high',
};
