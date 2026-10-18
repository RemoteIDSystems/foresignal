//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/source_access.dart';
import 'package:foresignal_api/src/model/budget.dart';
import 'package:foresignal_api/src/model/history_tier.dart';
import 'package:foresignal_api/src/model/health_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceSummary {
  /// Returns a new [SourceSummary] instance.
  SourceSummary({

    required  this.id,

    required  this.name,

    required  this.kinds,

    required  this.mode,

     this.custom,

    required  this.enabled,

    required  this.status,

     this.credentialsRequired,

     this.credentialsSet,

     this.interval,

     this.budget,

     this.budgetUsedToday,

     this.lastSuccessAt,

     this.historyTier,

     this.licence,

     this.termsUrl,

     this.lastDataAt,

     this.access,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



      /// Collection mode; `derived` sources are fed by ChangeBus or frame events and never polled
  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final SourceSummaryModeEnum mode;



  @JsonKey(
    
    name: r'custom',
    required: false,
    includeIfNull: false,
  )


  final bool? custom;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HealthStatus status;



      /// True if any credential field is required
  @JsonKey(
    
    name: r'credentialsRequired',
    required: false,
    includeIfNull: false,
  )


  final bool? credentialsRequired;



  @JsonKey(
    
    name: r'credentialsSet',
    required: false,
    includeIfNull: false,
  )


  final bool? credentialsSet;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final String? interval;



  @JsonKey(
    
    name: r'budget',
    required: false,
    includeIfNull: false,
  )


  final Budget? budget;



  @JsonKey(
    
    name: r'budgetUsedToday',
    required: false,
    includeIfNull: false,
  )


  final int? budgetUsedToday;



  @JsonKey(
    
    name: r'lastSuccessAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSuccessAt;



  @JsonKey(
    
    name: r'historyTier',
    required: false,
    includeIfNull: false,
  )


  final HistoryTier? historyTier;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



  @JsonKey(
    
    name: r'termsUrl',
    required: false,
    includeIfNull: false,
  )


  final String? termsUrl;



      /// Last fetch or flush that returned records (never a 304)
  @JsonKey(
    
    name: r'lastDataAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastDataAt;



  @JsonKey(
    
    name: r'access',
    required: false,
    includeIfNull: false,
  )


  final SourceAccess? access;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceSummary &&
      other.id == id &&
      other.name == name &&
      other.kinds == kinds &&
      other.mode == mode &&
      other.custom == custom &&
      other.enabled == enabled &&
      other.status == status &&
      other.credentialsRequired == credentialsRequired &&
      other.credentialsSet == credentialsSet &&
      other.interval == interval &&
      other.budget == budget &&
      other.budgetUsedToday == budgetUsedToday &&
      other.lastSuccessAt == lastSuccessAt &&
      other.historyTier == historyTier &&
      other.licence == licence &&
      other.termsUrl == termsUrl &&
      other.lastDataAt == lastDataAt &&
      other.access == access;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        kinds.hashCode +
        mode.hashCode +
        custom.hashCode +
        enabled.hashCode +
        status.hashCode +
        credentialsRequired.hashCode +
        credentialsSet.hashCode +
        interval.hashCode +
        budget.hashCode +
        budgetUsedToday.hashCode +
        (lastSuccessAt == null ? 0 : lastSuccessAt.hashCode) +
        historyTier.hashCode +
        (licence == null ? 0 : licence.hashCode) +
        (termsUrl == null ? 0 : termsUrl.hashCode) +
        (lastDataAt == null ? 0 : lastDataAt.hashCode) +
        access.hashCode;

  factory SourceSummary.fromJson(Map<String, dynamic> json) => _$SourceSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$SourceSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Collection mode; `derived` sources are fed by ChangeBus or frame events and never polled
enum SourceSummaryModeEnum {
@JsonValue(r'polling')
polling(r'polling'),
@JsonValue(r'streaming')
streaming(r'streaming'),
@JsonValue(r'push')
push(r'push'),
@JsonValue(r'derived')
derived(r'derived');

const SourceSummaryModeEnum(this.value);

final String value;

@override
String toString() => value;
}


