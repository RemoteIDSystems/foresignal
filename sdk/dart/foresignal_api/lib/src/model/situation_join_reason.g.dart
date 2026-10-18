// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_join_reason.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationJoinReasonCWProxy {
  SituationJoinReason method(SituationJoinReasonMethodEnum method);

  SituationJoinReason score(num? score);

  SituationJoinReason components(SituationJoinReasonComponents? components);

  SituationJoinReason linkerVersion(String? linkerVersion);

  SituationJoinReason actor(String? actor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationJoinReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationJoinReason(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationJoinReason call({
    SituationJoinReasonMethodEnum method,
    num? score,
    SituationJoinReasonComponents? components,
    String? linkerVersion,
    String? actor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationJoinReason.copyWith(...)` or call `instanceOfSituationJoinReason.copyWith.fieldName(value)` for a single field.
class _$SituationJoinReasonCWProxyImpl implements _$SituationJoinReasonCWProxy {
  const _$SituationJoinReasonCWProxyImpl(this._value);

  final SituationJoinReason _value;

  @override
  SituationJoinReason method(SituationJoinReasonMethodEnum method) =>
      call(method: method);

  @override
  SituationJoinReason score(num? score) => call(score: score);

  @override
  SituationJoinReason components(SituationJoinReasonComponents? components) =>
      call(components: components);

  @override
  SituationJoinReason linkerVersion(String? linkerVersion) =>
      call(linkerVersion: linkerVersion);

  @override
  SituationJoinReason actor(String? actor) => call(actor: actor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationJoinReason(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationJoinReason(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationJoinReason call({
    Object? method = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? components = const $CopyWithPlaceholder(),
    Object? linkerVersion = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
  }) {
    return SituationJoinReason(
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as SituationJoinReasonMethodEnum,
      score: score == const $CopyWithPlaceholder()
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num?,
      components: components == const $CopyWithPlaceholder()
          ? _value.components
          // ignore: cast_nullable_to_non_nullable
          : components as SituationJoinReasonComponents?,
      linkerVersion: linkerVersion == const $CopyWithPlaceholder()
          ? _value.linkerVersion
          // ignore: cast_nullable_to_non_nullable
          : linkerVersion as String?,
      actor: actor == const $CopyWithPlaceholder()
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as String?,
    );
  }
}

extension $SituationJoinReasonCopyWith on SituationJoinReason {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationJoinReason.copyWith(...)` or `instanceOfSituationJoinReason.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationJoinReasonCWProxy get copyWith =>
      _$SituationJoinReasonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationJoinReason _$SituationJoinReasonFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationJoinReason', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['method']);
      final val = SituationJoinReason(
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$SituationJoinReasonMethodEnumEnumMap, v),
        ),
        score: $checkedConvert('score', (v) => v as num?),
        components: $checkedConvert(
          'components',
          (v) => v == null
              ? null
              : SituationJoinReasonComponents.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        linkerVersion: $checkedConvert('linkerVersion', (v) => v as String?),
        actor: $checkedConvert('actor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationJoinReasonToJson(
  SituationJoinReason instance,
) => <String, dynamic>{
  'method': _$SituationJoinReasonMethodEnumEnumMap[instance.method]!,
  'score': ?instance.score,
  'components': ?instance.components?.toJson(),
  'linkerVersion': ?instance.linkerVersion,
  'actor': ?instance.actor,
};

const _$SituationJoinReasonMethodEnumEnumMap = {
  SituationJoinReasonMethodEnum.linker: 'linker',
  SituationJoinReasonMethodEnum.seed: 'seed',
  SituationJoinReasonMethodEnum.operator_: 'operator',
  SituationJoinReasonMethodEnum.agent: 'agent',
  SituationJoinReasonMethodEnum.analyst: 'analyst',
};
