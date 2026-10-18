//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_social_volume_selector.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of3.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of6.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of2.dart';
import 'package:foresignal_api/src/model/chronicle_external_stream_selector.dart';
import 'package:foresignal_api/src/model/condition.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of1.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of4.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of7.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of5.dart';
import 'package:foresignal_api/src/model/chronicle_layer_selector_one_of8.dart';
import 'package:foresignal_api/src/model/chronicle_social_volume_selector_scopes_inner.dart';
import 'package:foresignal_api/src/model/chronicle_social_narratives_selector.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelector {
  /// Returns a new [ChronicleLayerSelector] instance.
  ChronicleLayerSelector({

    required  this.type,

     this.sources,

     this.kinds,

     this.conditions,

    required  this.viewId,

    required  this.watchlistId,

    required  this.entityIds,

    required  this.situationIds,

     this.findingTypes,

     this.families,

    required  this.refs,

    required  this.productIds,

     this.ruleIds,

     this.tags,

     this.topicIds,

     this.placeIds,

     this.regionCodes,

     this.statuses,

     this.keepPostText = false,

    required  this.scopes,

     this.platforms,

     this.metrics,

    required  this.sourceId,

    required  this.streamId,

     this.filter,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorTypeEnum type;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// 017 condition grammar
  @JsonKey(
    
    name: r'conditions',
    required: false,
    includeIfNull: false,
  )


  final List<Condition>? conditions;



  @JsonKey(
    
    name: r'viewId',
    required: true,
    includeIfNull: false,
  )


  final String viewId;



  @JsonKey(
    
    name: r'watchlistId',
    required: true,
    includeIfNull: false,
  )


  final String watchlistId;



  @JsonKey(
    
    name: r'entityIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> entityIds;



  @JsonKey(
    
    name: r'situationIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> situationIds;



  @JsonKey(
    
    name: r'findingTypes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? findingTypes;



  @JsonKey(
    
    name: r'families',
    required: false,
    includeIfNull: false,
  )


  final List<String>? families;



  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



  @JsonKey(
    
    name: r'productIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> productIds;



  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'statuses',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleLayerSelectorStatusesEnum>? statuses;



      /// Keep member post text in chronicle storage (else claim summaries and counts only)
  @JsonKey(
    defaultValue: false,
    name: r'keepPostText',
    required: false,
    includeIfNull: false,
  )


  final bool? keepPostText;



  @JsonKey(
    
    name: r'scopes',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSocialVolumeSelectorScopesInner> scopes;



  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<String>? platforms;



  @JsonKey(
    
    name: r'metrics',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleLayerSelectorMetricsEnum>? metrics;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? filter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelector &&
      other.type == type &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.conditions == conditions &&
      other.viewId == viewId &&
      other.watchlistId == watchlistId &&
      other.entityIds == entityIds &&
      other.situationIds == situationIds &&
      other.findingTypes == findingTypes &&
      other.families == families &&
      other.refs == refs &&
      other.productIds == productIds &&
      other.ruleIds == ruleIds &&
      other.tags == tags &&
      other.topicIds == topicIds &&
      other.placeIds == placeIds &&
      other.regionCodes == regionCodes &&
      other.statuses == statuses &&
      other.keepPostText == keepPostText &&
      other.scopes == scopes &&
      other.platforms == platforms &&
      other.metrics == metrics &&
      other.sourceId == sourceId &&
      other.streamId == streamId &&
      other.filter == filter;

    @override
    int get hashCode =>
        type.hashCode +
        sources.hashCode +
        kinds.hashCode +
        conditions.hashCode +
        viewId.hashCode +
        watchlistId.hashCode +
        entityIds.hashCode +
        situationIds.hashCode +
        findingTypes.hashCode +
        families.hashCode +
        refs.hashCode +
        productIds.hashCode +
        ruleIds.hashCode +
        tags.hashCode +
        topicIds.hashCode +
        placeIds.hashCode +
        regionCodes.hashCode +
        statuses.hashCode +
        keepPostText.hashCode +
        scopes.hashCode +
        platforms.hashCode +
        metrics.hashCode +
        sourceId.hashCode +
        streamId.hashCode +
        (filter == null ? 0 : filter.hashCode);

  factory ChronicleLayerSelector.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorTypeEnum {
@JsonValue(r'records')
records(r'records'),
@JsonValue(r'saved-view')
savedView(r'saved-view'),
@JsonValue(r'watchlist')
watchlist(r'watchlist'),
@JsonValue(r'entities')
entities(r'entities'),
@JsonValue(r'situations')
situations(r'situations'),
@JsonValue(r'findings')
findings(r'findings'),
@JsonValue(r'series')
series(r'series'),
@JsonValue(r'raster-products')
rasterProducts(r'raster-products'),
@JsonValue(r'alerts')
alerts(r'alerts'),
@JsonValue(r'social-narratives')
socialNarratives(r'social-narratives'),
@JsonValue(r'social-volume')
socialVolume(r'social-volume'),
@JsonValue(r'external-stream')
externalStream(r'external-stream');

const ChronicleLayerSelectorTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLayerSelectorStatusesEnum {
@JsonValue(r'unassessed')
unassessed(r'unassessed'),
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'unobserved')
unobserved(r'unobserved'),
@JsonValue(r'contradicted')
contradicted(r'contradicted'),
@JsonValue(r'mixed')
mixed(r'mixed'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const ChronicleLayerSelectorStatusesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleLayerSelectorMetricsEnum {
@JsonValue(r'posts')
posts(r'posts'),
@JsonValue(r'uniqueAuthors')
uniqueAuthors(r'uniqueAuthors'),
@JsonValue(r'reposts')
reposts(r'reposts'),
@JsonValue(r'engagement')
engagement(r'engagement');

const ChronicleLayerSelectorMetricsEnum(this.value);

final String value;

@override
String toString() => value;
}


