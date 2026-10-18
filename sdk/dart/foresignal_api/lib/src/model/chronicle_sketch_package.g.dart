// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_sketch_package.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSketchPackageCWProxy {
  ChronicleSketchPackage sketchId(String sketchId);

  ChronicleSketchPackage instanceId(String instanceId);

  ChronicleSketchPackage networkId(String networkId);

  ChronicleSketchPackage keyTypes(
    List<ChronicleSketchPackageKeyTypesEnum> keyTypes,
  );

  ChronicleSketchPackage minhash(Map<String, String> minhash);

  ChronicleSketchPackage sizeBand(Map<String, int> sizeBand);

  ChronicleSketchPackage captureSpan(
    ChronicleSketchPackageCaptureSpan captureSpan,
  );

  ChronicleSketchPackage title(String? title);

  ChronicleSketchPackage ownerHandle(String? ownerHandle);

  ChronicleSketchPackage tlp(Tlp tlp);

  ChronicleSketchPackage issuedAt(DateTime issuedAt);

  ChronicleSketchPackage expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchPackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchPackage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSketchPackage call({
    String sketchId,
    String instanceId,
    String networkId,
    List<ChronicleSketchPackageKeyTypesEnum> keyTypes,
    Map<String, String> minhash,
    Map<String, int> sizeBand,
    ChronicleSketchPackageCaptureSpan captureSpan,
    String? title,
    String? ownerHandle,
    Tlp tlp,
    DateTime issuedAt,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSketchPackage.copyWith(...)` or call `instanceOfChronicleSketchPackage.copyWith.fieldName(value)` for a single field.
class _$ChronicleSketchPackageCWProxyImpl
    implements _$ChronicleSketchPackageCWProxy {
  const _$ChronicleSketchPackageCWProxyImpl(this._value);

  final ChronicleSketchPackage _value;

  @override
  ChronicleSketchPackage sketchId(String sketchId) => call(sketchId: sketchId);

  @override
  ChronicleSketchPackage instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  ChronicleSketchPackage networkId(String networkId) =>
      call(networkId: networkId);

  @override
  ChronicleSketchPackage keyTypes(
    List<ChronicleSketchPackageKeyTypesEnum> keyTypes,
  ) => call(keyTypes: keyTypes);

  @override
  ChronicleSketchPackage minhash(Map<String, String> minhash) =>
      call(minhash: minhash);

  @override
  ChronicleSketchPackage sizeBand(Map<String, int> sizeBand) =>
      call(sizeBand: sizeBand);

  @override
  ChronicleSketchPackage captureSpan(
    ChronicleSketchPackageCaptureSpan captureSpan,
  ) => call(captureSpan: captureSpan);

  @override
  ChronicleSketchPackage title(String? title) => call(title: title);

  @override
  ChronicleSketchPackage ownerHandle(String? ownerHandle) =>
      call(ownerHandle: ownerHandle);

  @override
  ChronicleSketchPackage tlp(Tlp tlp) => call(tlp: tlp);

  @override
  ChronicleSketchPackage issuedAt(DateTime issuedAt) =>
      call(issuedAt: issuedAt);

  @override
  ChronicleSketchPackage expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSketchPackage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSketchPackage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSketchPackage call({
    Object? sketchId = const $CopyWithPlaceholder(),
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? networkId = const $CopyWithPlaceholder(),
    Object? keyTypes = const $CopyWithPlaceholder(),
    Object? minhash = const $CopyWithPlaceholder(),
    Object? sizeBand = const $CopyWithPlaceholder(),
    Object? captureSpan = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? ownerHandle = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? issuedAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSketchPackage(
      sketchId: sketchId == const $CopyWithPlaceholder() || sketchId == null
          ? _value.sketchId
          // ignore: cast_nullable_to_non_nullable
          : sketchId as String,
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      networkId: networkId == const $CopyWithPlaceholder() || networkId == null
          ? _value.networkId
          // ignore: cast_nullable_to_non_nullable
          : networkId as String,
      keyTypes: keyTypes == const $CopyWithPlaceholder() || keyTypes == null
          ? _value.keyTypes
          // ignore: cast_nullable_to_non_nullable
          : keyTypes as List<ChronicleSketchPackageKeyTypesEnum>,
      minhash: minhash == const $CopyWithPlaceholder() || minhash == null
          ? _value.minhash
          // ignore: cast_nullable_to_non_nullable
          : minhash as Map<String, String>,
      sizeBand: sizeBand == const $CopyWithPlaceholder() || sizeBand == null
          ? _value.sizeBand
          // ignore: cast_nullable_to_non_nullable
          : sizeBand as Map<String, int>,
      captureSpan:
          captureSpan == const $CopyWithPlaceholder() || captureSpan == null
          ? _value.captureSpan
          // ignore: cast_nullable_to_non_nullable
          : captureSpan as ChronicleSketchPackageCaptureSpan,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      ownerHandle: ownerHandle == const $CopyWithPlaceholder()
          ? _value.ownerHandle
          // ignore: cast_nullable_to_non_nullable
          : ownerHandle as String?,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      issuedAt: issuedAt == const $CopyWithPlaceholder() || issuedAt == null
          ? _value.issuedAt
          // ignore: cast_nullable_to_non_nullable
          : issuedAt as DateTime,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $ChronicleSketchPackageCopyWith on ChronicleSketchPackage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSketchPackage.copyWith(...)` or `instanceOfChronicleSketchPackage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSketchPackageCWProxy get copyWith =>
      _$ChronicleSketchPackageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSketchPackage _$ChronicleSketchPackageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSketchPackage', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'sketchId',
      'instanceId',
      'networkId',
      'keyTypes',
      'minhash',
      'sizeBand',
      'captureSpan',
      'tlp',
      'issuedAt',
    ],
  );
  final val = ChronicleSketchPackage(
    sketchId: $checkedConvert('sketchId', (v) => v as String),
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    networkId: $checkedConvert('networkId', (v) => v as String),
    keyTypes: $checkedConvert(
      'keyTypes',
      (v) => (v as List<dynamic>)
          .map(
            (e) => $enumDecode(_$ChronicleSketchPackageKeyTypesEnumEnumMap, e),
          )
          .toList(),
    ),
    minhash: $checkedConvert(
      'minhash',
      (v) => Map<String, String>.from(v as Map),
    ),
    sizeBand: $checkedConvert(
      'sizeBand',
      (v) => Map<String, int>.from(v as Map),
    ),
    captureSpan: $checkedConvert(
      'captureSpan',
      (v) =>
          ChronicleSketchPackageCaptureSpan.fromJson(v as Map<String, dynamic>),
    ),
    title: $checkedConvert('title', (v) => v as String?),
    ownerHandle: $checkedConvert('ownerHandle', (v) => v as String?),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    issuedAt: $checkedConvert('issuedAt', (v) => DateTime.parse(v as String)),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSketchPackageToJson(
  ChronicleSketchPackage instance,
) => <String, dynamic>{
  'sketchId': instance.sketchId,
  'instanceId': instance.instanceId,
  'networkId': instance.networkId,
  'keyTypes': instance.keyTypes
      .map((e) => _$ChronicleSketchPackageKeyTypesEnumEnumMap[e]!)
      .toList(),
  'minhash': instance.minhash,
  'sizeBand': instance.sizeBand,
  'captureSpan': instance.captureSpan.toJson(),
  'title': ?instance.title,
  'ownerHandle': ?instance.ownerHandle,
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'issuedAt': instance.issuedAt.toIso8601String(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};

const _$ChronicleSketchPackageKeyTypesEnumEnumMap = {
  ChronicleSketchPackageKeyTypesEnum.evidence: 'evidence',
  ChronicleSketchPackageKeyTypesEnum.entities: 'entities',
  ChronicleSketchPackageKeyTypesEnum.areaTime: 'area-time',
  ChronicleSketchPackageKeyTypesEnum.topics: 'topics',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
