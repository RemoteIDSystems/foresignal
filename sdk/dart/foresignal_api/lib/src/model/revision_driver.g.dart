// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'revision_driver.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RevisionDriverCWProxy {
  RevisionDriver kind(RevisionDriverKindEnum kind);

  RevisionDriver id(String? id);

  RevisionDriver ref(String? ref);

  RevisionDriver hypothesisId(String hypothesisId);

  RevisionDriver signalFamily(String? signalFamily);

  RevisionDriver lr(num? lr);

  RevisionDriver lnLr(num? lnLr);

  RevisionDriver pDetect(num? pDetect);

  RevisionDriver applied(num applied);

  RevisionDriver suppressed(bool suppressed);

  RevisionDriver suppressionReason(
    RevisionDriverSuppressionReasonEnum? suppressionReason,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RevisionDriver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RevisionDriver(...).copyWith(id: 12, name: "My name")
  /// ```
  RevisionDriver call({
    RevisionDriverKindEnum kind,
    String? id,
    String? ref,
    String hypothesisId,
    String? signalFamily,
    num? lr,
    num? lnLr,
    num? pDetect,
    num applied,
    bool suppressed,
    RevisionDriverSuppressionReasonEnum? suppressionReason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRevisionDriver.copyWith(...)` or call `instanceOfRevisionDriver.copyWith.fieldName(value)` for a single field.
class _$RevisionDriverCWProxyImpl implements _$RevisionDriverCWProxy {
  const _$RevisionDriverCWProxyImpl(this._value);

  final RevisionDriver _value;

  @override
  RevisionDriver kind(RevisionDriverKindEnum kind) => call(kind: kind);

  @override
  RevisionDriver id(String? id) => call(id: id);

  @override
  RevisionDriver ref(String? ref) => call(ref: ref);

  @override
  RevisionDriver hypothesisId(String hypothesisId) =>
      call(hypothesisId: hypothesisId);

  @override
  RevisionDriver signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  RevisionDriver lr(num? lr) => call(lr: lr);

  @override
  RevisionDriver lnLr(num? lnLr) => call(lnLr: lnLr);

  @override
  RevisionDriver pDetect(num? pDetect) => call(pDetect: pDetect);

  @override
  RevisionDriver applied(num applied) => call(applied: applied);

  @override
  RevisionDriver suppressed(bool suppressed) => call(suppressed: suppressed);

  @override
  RevisionDriver suppressionReason(
    RevisionDriverSuppressionReasonEnum? suppressionReason,
  ) => call(suppressionReason: suppressionReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RevisionDriver(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RevisionDriver(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RevisionDriver call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? hypothesisId = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? lr = const $CopyWithPlaceholder(),
    Object? lnLr = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? applied = const $CopyWithPlaceholder(),
    Object? suppressed = const $CopyWithPlaceholder(),
    Object? suppressionReason = const $CopyWithPlaceholder(),
  }) {
    return RevisionDriver(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as RevisionDriverKindEnum,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      hypothesisId:
          hypothesisId == const $CopyWithPlaceholder() || hypothesisId == null
          ? _value.hypothesisId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisId as String,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      lr: lr == const $CopyWithPlaceholder()
          ? _value.lr
          // ignore: cast_nullable_to_non_nullable
          : lr as num?,
      lnLr: lnLr == const $CopyWithPlaceholder()
          ? _value.lnLr
          // ignore: cast_nullable_to_non_nullable
          : lnLr as num?,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
      applied: applied == const $CopyWithPlaceholder() || applied == null
          ? _value.applied
          // ignore: cast_nullable_to_non_nullable
          : applied as num,
      suppressed:
          suppressed == const $CopyWithPlaceholder() || suppressed == null
          ? _value.suppressed
          // ignore: cast_nullable_to_non_nullable
          : suppressed as bool,
      suppressionReason: suppressionReason == const $CopyWithPlaceholder()
          ? _value.suppressionReason
          // ignore: cast_nullable_to_non_nullable
          : suppressionReason as RevisionDriverSuppressionReasonEnum?,
    );
  }
}

extension $RevisionDriverCopyWith on RevisionDriver {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRevisionDriver.copyWith(...)` or `instanceOfRevisionDriver.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RevisionDriverCWProxy get copyWith => _$RevisionDriverCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RevisionDriver _$RevisionDriverFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RevisionDriver', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['kind', 'hypothesisId', 'applied', 'suppressed'],
      );
      final val = RevisionDriver(
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$RevisionDriverKindEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String?),
        ref: $checkedConvert('ref', (v) => v as String?),
        hypothesisId: $checkedConvert('hypothesisId', (v) => v as String),
        signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
        lr: $checkedConvert('lr', (v) => v as num?),
        lnLr: $checkedConvert('lnLr', (v) => v as num?),
        pDetect: $checkedConvert('pDetect', (v) => v as num?),
        applied: $checkedConvert('applied', (v) => v as num),
        suppressed: $checkedConvert('suppressed', (v) => v as bool),
        suppressionReason: $checkedConvert(
          'suppressionReason',
          (v) => $enumDecodeNullable(
            _$RevisionDriverSuppressionReasonEnumEnumMap,
            v,
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RevisionDriverToJson(
  RevisionDriver instance,
) => <String, dynamic>{
  'kind': _$RevisionDriverKindEnumEnumMap[instance.kind]!,
  'id': ?instance.id,
  'ref': ?instance.ref,
  'hypothesisId': instance.hypothesisId,
  'signalFamily': ?instance.signalFamily,
  'lr': ?instance.lr,
  'lnLr': ?instance.lnLr,
  'pDetect': ?instance.pDetect,
  'applied': instance.applied,
  'suppressed': instance.suppressed,
  'suppressionReason':
      ?_$RevisionDriverSuppressionReasonEnumEnumMap[instance.suppressionReason],
};

const _$RevisionDriverKindEnumEnumMap = {
  RevisionDriverKindEnum.indicator: 'indicator',
  RevisionDriverKindEnum.evidence: 'evidence',
  RevisionDriverKindEnum.baseRate: 'base-rate',
  RevisionDriverKindEnum.review: 'review',
  RevisionDriverKindEnum.operator_: 'operator',
};

const _$RevisionDriverSuppressionReasonEnumEnumMap = {
  RevisionDriverSuppressionReasonEnum.sameEvidenceGroup: 'same-evidence-group',
  RevisionDriverSuppressionReasonEnum.sameFamilyDay: 'same-family-day',
  RevisionDriverSuppressionReasonEnum.damped: 'damped',
  RevisionDriverSuppressionReasonEnum.capped: 'capped',
};
