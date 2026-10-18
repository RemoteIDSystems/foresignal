// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseCWProxy {
  BenchmarkCase id(String id);

  BenchmarkCase name(String name);

  BenchmarkCase description(String? description);

  BenchmarkCase windowFrom(DateTime windowFrom);

  BenchmarkCase windowTo(DateTime windowTo);

  BenchmarkCase regionCodes(List<String> regionCodes);

  BenchmarkCase expectations(BenchmarkCaseExpectations expectations);

  BenchmarkCase source_(BenchmarkCaseSource_Enum source_);

  BenchmarkCase createdBy(String? createdBy);

  BenchmarkCase createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCase(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCase call({
    String id,
    String name,
    String? description,
    DateTime windowFrom,
    DateTime windowTo,
    List<String> regionCodes,
    BenchmarkCaseExpectations expectations,
    BenchmarkCaseSource_Enum source_,
    String? createdBy,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCase.copyWith(...)` or call `instanceOfBenchmarkCase.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseCWProxyImpl implements _$BenchmarkCaseCWProxy {
  const _$BenchmarkCaseCWProxyImpl(this._value);

  final BenchmarkCase _value;

  @override
  BenchmarkCase id(String id) => call(id: id);

  @override
  BenchmarkCase name(String name) => call(name: name);

  @override
  BenchmarkCase description(String? description) =>
      call(description: description);

  @override
  BenchmarkCase windowFrom(DateTime windowFrom) => call(windowFrom: windowFrom);

  @override
  BenchmarkCase windowTo(DateTime windowTo) => call(windowTo: windowTo);

  @override
  BenchmarkCase regionCodes(List<String> regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  BenchmarkCase expectations(BenchmarkCaseExpectations expectations) =>
      call(expectations: expectations);

  @override
  BenchmarkCase source_(BenchmarkCaseSource_Enum source_) =>
      call(source_: source_);

  @override
  BenchmarkCase createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  BenchmarkCase createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCase(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCase(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCase call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? windowFrom = const $CopyWithPlaceholder(),
    Object? windowTo = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? expectations = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCase(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      windowFrom:
          windowFrom == const $CopyWithPlaceholder() || windowFrom == null
          ? _value.windowFrom
          // ignore: cast_nullable_to_non_nullable
          : windowFrom as DateTime,
      windowTo: windowTo == const $CopyWithPlaceholder() || windowTo == null
          ? _value.windowTo
          // ignore: cast_nullable_to_non_nullable
          : windowTo as DateTime,
      regionCodes:
          regionCodes == const $CopyWithPlaceholder() || regionCodes == null
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>,
      expectations:
          expectations == const $CopyWithPlaceholder() || expectations == null
          ? _value.expectations
          // ignore: cast_nullable_to_non_nullable
          : expectations as BenchmarkCaseExpectations,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as BenchmarkCaseSource_Enum,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $BenchmarkCaseCopyWith on BenchmarkCase {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCase.copyWith(...)` or `instanceOfBenchmarkCase.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseCWProxy get copyWith => _$BenchmarkCaseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCase _$BenchmarkCaseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BenchmarkCase', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'windowFrom',
      'windowTo',
      'regionCodes',
      'expectations',
      'source',
      'createdAt',
    ],
  );
  final val = BenchmarkCase(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    windowFrom: $checkedConvert(
      'windowFrom',
      (v) => DateTime.parse(v as String),
    ),
    windowTo: $checkedConvert('windowTo', (v) => DateTime.parse(v as String)),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    expectations: $checkedConvert(
      'expectations',
      (v) => BenchmarkCaseExpectations.fromJson(v as Map<String, dynamic>),
    ),
    source_: $checkedConvert(
      'source',
      (v) => $enumDecode(_$BenchmarkCaseSource_EnumEnumMap, v),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$BenchmarkCaseToJson(BenchmarkCase instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': ?instance.description,
      'windowFrom': instance.windowFrom.toIso8601String(),
      'windowTo': instance.windowTo.toIso8601String(),
      'regionCodes': instance.regionCodes,
      'expectations': instance.expectations.toJson(),
      'source': _$BenchmarkCaseSource_EnumEnumMap[instance.source_]!,
      'createdBy': ?instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
    };

const _$BenchmarkCaseSource_EnumEnumMap = {
  BenchmarkCaseSource_Enum.operator_: 'operator',
  BenchmarkCaseSource_Enum.agent: 'agent',
  BenchmarkCaseSource_Enum.auto: 'auto',
};
